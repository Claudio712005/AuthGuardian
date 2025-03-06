package com.clau.authguardian.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.clau.authguardian.dto.request.AuthRequestDTO;
import com.clau.authguardian.dto.request.RefreshTokenRequestDTO;
import com.clau.authguardian.dto.response.TokenResponseDTO;
import com.clau.authguardian.model.Usuario;
import com.clau.authguardian.repository.UsuarioRepository;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService implements UserDetailsService {

  private final JwtService jwtService;
  private final UsuarioRepository repository;
  private final RedisService redisService;
  private final EmailService emailService;

  private static final Logger LOGGER = LoggerFactory.getLogger(UsuarioService.class);

  private static final int MAX_ATTEMPTS = 5;
  private static final int ATTEMPT_TIMEOUT = 10 * 60 * 1000;

  public UsuarioService(JwtService jwtService, UsuarioRepository repository, RedisService redisService, EmailService emailService) {
    this.jwtService = jwtService;
    this.repository = repository;
    this.redisService = redisService;
    this.emailService = emailService;
  }

  public TokenResponseDTO authenticate(AuthRequestDTO authRequestDTO, AuthenticationManager authenticationManager, HttpServletRequest request) {
    String username = authRequestDTO.getEmail();
    String ipAddress = getClientIp(request);

    String key = "login_attempts:" + ipAddress + ":" + username;

    String attemptsString = redisService.getFromRedis(key);
    int attempts = attemptsString != null ? Integer.parseInt(attemptsString) : 0;

    if (attempts >= MAX_ATTEMPTS) {
      long lastAttemptTime = Long.parseLong(redisService.getFromRedis(key + ":time"));
      if (System.currentTimeMillis() - lastAttemptTime < ATTEMPT_TIMEOUT) {
        throw new RuntimeException("Muitas tentativas de login. Tente novamente mais tarde.");
      }
    }

    try {
      Authentication authentication = authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(authRequestDTO.getEmail(), authRequestDTO.getSenha())
      );

      Usuario userDetails = (Usuario) authentication.getPrincipal();

      redisService.removeFromRedis(key);

      String accessToken = jwtService.generateAccessToken(userDetails);
      String refreshToken = jwtService.generateRefreshToken(userDetails);

      return new TokenResponseDTO(accessToken, refreshToken);
    } catch (BadCredentialsException e) {
      redisService.incrementInRedis(key);
      redisService.setInRedis(key + ":time", String.valueOf(System.currentTimeMillis()));

      throw new BadCredentialsException("Credenciais inválidas.");
    }
  }

  public TokenResponseDTO refreshToken(RefreshTokenRequestDTO request) {
    String refreshToken = request.getRefreshToken();

    if (redisService.isTokenBlacklisted(refreshToken)) {
      throw new RuntimeException("Token invalidado, por favor faça login novamente.");
    }

    try {
      String email = jwtService.validateRefreshToken(refreshToken);
      if (email.isEmpty()) {
        throw new JWTVerificationException("Token inválido ou expirado");
      }

      Usuario usuario = (Usuario) loadUserByUsername(email);

      String newAccessToken = jwtService.generateAccessToken(usuario);
      String newRefreshToken = jwtService.generateRefreshToken(usuario);

      return new TokenResponseDTO(newAccessToken, newRefreshToken);
    } catch (JWTVerificationException e) {
      throw new RuntimeException("Falha ao validar refresh token", e);
    }
  }

  public void logout(String token) {
    try {
      String email = jwtService.validateToken(token);

      if (email != null && !email.isEmpty()) {
        redisService.addToBlacklist(token);
      } else {
        LOGGER.error("Token já expirado ou inválido, não será adicionado na blacklist.");
      }
    } catch (JWTVerificationException e) {
      LOGGER.error("Erro ao invalidar token", e);
    }
  }

  public void validateToken(String token) {
    try {
      String subj = jwtService.validateToken(token);
      if (StringUtils.isBlank(subj)) {
        throw new JWTVerificationException("Token inválido");
      }

      if (redisService.isTokenBlacklisted(token)) {
        throw new JWTVerificationException("Token inválido");
      }
    } catch (JWTVerificationException e) {
      throw e;
    }
  }

  public void forgotPassword(String email) {
    Usuario usuario = (Usuario) loadUserByUsername(email);
    String forgotPasswordToken = jwtService.generateForgotPasswordToken(usuario);

    emailService.sendForgotPasswordEmail(email, usuario.getNome(), forgotPasswordToken);
  }

  public void resetPassword(String password, String retypedPassword, String token) {
    if (!password.equals(retypedPassword)) {
      throw new RuntimeException("Senhas não conferem");
    }

    if (redisService.isTokenBlacklisted(token)) {
      throw new RuntimeException("Token inválido ou expirado");
    }

    String email = jwtService.validateForgotPasswordToken(token);
    if (email.isEmpty()) {
      throw new JWTVerificationException("Token inválido ou expirado");
    }

    Usuario usuario = (Usuario) loadUserByUsername(email);

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    String encodedPassword = passwordEncoder.encode(password);

    usuario.setSenha(encodedPassword);
    repository.save(usuario);

    redisService.addToBlacklist(token);
  }

  private String getClientIp(HttpServletRequest request) {
    String remoteAddr = request.getHeader("X-Forwarded-For");
    if (remoteAddr == null || remoteAddr.isEmpty() || "unknown".equalsIgnoreCase(remoteAddr)) {
      remoteAddr = request.getRemoteAddr();
    }
    return remoteAddr;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return repository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com email: " + username));
  }
}
