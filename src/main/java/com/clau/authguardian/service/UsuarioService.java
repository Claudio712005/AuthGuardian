package com.clau.authguardian.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.clau.authguardian.dto.request.AuthRequestDTO;
import com.clau.authguardian.dto.request.RefreshTokenRequestDTO;
import com.clau.authguardian.dto.response.TokenResponseDTO;
import com.clau.authguardian.model.Usuario;
import com.clau.authguardian.repository.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService implements UserDetailsService {

  private final JwtService jwtService;
  private final UsuarioRepository repository;

  public UsuarioService(JwtService jwtService, UsuarioRepository repository) {
    this.jwtService = jwtService;
    this.repository = repository;
  }

  private final String SECRET_KEY = "chaveSecretaSuperSegura";

  public TokenResponseDTO authenticate(AuthRequestDTO authRequestDTO, AuthenticationManager authenticationManager) {
    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(authRequestDTO.getEmail(), authRequestDTO.getSenha())
    );

    Usuario userDetails = (Usuario) authentication.getPrincipal();
    String accessToken = jwtService.generateAccessToken(userDetails);
    String refreshToken = jwtService.generateRefreshToken(userDetails);

    return new TokenResponseDTO(accessToken, refreshToken);
  }

  public TokenResponseDTO refreshToken(RefreshTokenRequestDTO request) {
    try {
      String email = jwtService.validateRefreshToken(request.getRefreshToken());

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

  public void logout() {

  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return repository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com email: " + username));
  }
}
