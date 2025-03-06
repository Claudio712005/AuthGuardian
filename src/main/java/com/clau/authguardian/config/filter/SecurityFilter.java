package com.clau.authguardian.config.filter;

import com.clau.authguardian.repository.UsuarioRepository;
import com.clau.authguardian.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class SecurityFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

  private final JwtService jwtService;
  private final UsuarioRepository userRepository;

  public SecurityFilter(JwtService jwtService, UsuarioRepository userRepository) {
    this.jwtService = jwtService;
    this.userRepository = userRepository;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
          throws ServletException, IOException {

    var token = this.recoverToken(request);

    if (token != null) {
      logger.info("Token recuperado: {}", token);

      var login = jwtService.validateToken(token);

      if (login != null) {
        logger.info("Token válido, login: {}", login);

        Optional<UserDetails> user = userRepository.findByEmail(login);

        if (user.isPresent()) {
          var authentication = new UsernamePasswordAuthenticationToken(user.get(), null, user.get().getAuthorities());

          SecurityContextHolder.getContext().setAuthentication(authentication);
          logger.info("Usuário autenticado: {}", login);
        } else {
          logger.warn("Usuário não encontrado: {}", login);
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          filterChain.doFilter(request, response);
          return;
        }
      } else {
        logger.warn("Token inválido ou expirado");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        filterChain.doFilter(request, response);
        return;
      }
    } else {
      logger.warn("Nenhum token encontrado no cabeçalho Authorization");
    }

    filterChain.doFilter(request, response);
  }

  private String recoverToken(HttpServletRequest request) {
    var authHeader = request.getHeader("Authorization");
    if (authHeader == null)
      return null;
    return authHeader.replace("Bearer ", "");
  }
}
