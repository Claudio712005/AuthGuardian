package com.clau.authguardian.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.clau.authguardian.dto.request.AuthRequestDTO;
import com.clau.authguardian.dto.request.RefreshTokenRequestDTO;
import com.clau.authguardian.dto.response.TokenResponseDTO;
import com.clau.authguardian.service.JwtService;
import com.clau.authguardian.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/v1")
public class AuthController {

  private final UsuarioService usuarioService;
  private final AuthenticationManager authenticationManager;

  private static final Logger Logger = LoggerFactory.getLogger(AuthController.class);

  public AuthController(UsuarioService usuarioService, AuthenticationManager authenticationManager) {
    this.usuarioService = usuarioService;
    this.authenticationManager = authenticationManager;
  }

  @PostMapping("/login")
  public ResponseEntity<TokenResponseDTO> login(@RequestBody AuthRequestDTO authRequestDTO) {
    return ResponseEntity.ok().body(usuarioService.authenticate(authRequestDTO, authenticationManager));
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<TokenResponseDTO> refreshToken(@RequestBody RefreshTokenRequestDTO request) {
    TokenResponseDTO response = usuarioService.refreshToken(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("logout")
  public ResponseEntity logout(@RequestHeader("Authorization") String token) {
    if(token == null) {
      return ResponseEntity.badRequest().body("Token não informado");
    }

    usuarioService.logout(token.replace("Bearer ", ""));
    return ResponseEntity.ok().build();
  }

  @PostMapping("/validate")
  public ResponseEntity validate(@RequestHeader("Authorization") String token) {
    try {
      usuarioService.validateToken(token.replace("Bearer ", "").trim());
      return ResponseEntity.ok().build();
    } catch (JWTVerificationException e) {
      Logger.error("Token inválido ou expirado", e);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }

  @PostMapping("forgot-password")
  public void forgotPassword(@RequestParam String email) {
    usuarioService.forgotPassword(email);
  }

  @PostMapping("reset-password")
  public void resetPassword(@RequestParam String password, @RequestParam String retypedPassword, @RequestHeader("Authorization") String token) {
    usuarioService.resetPassword(password, retypedPassword, token.replace("Bearer ", "").trim());
  }
}
