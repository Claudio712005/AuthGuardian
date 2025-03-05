package com.clau.authguardian.controller;

import com.clau.authguardian.dto.request.AuthRequestDTO;
import com.clau.authguardian.dto.request.RefreshTokenRequestDTO;
import com.clau.authguardian.dto.response.TokenResponseDTO;
import com.clau.authguardian.service.JwtService;
import com.clau.authguardian.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/v1")
public class AuthController {

  private final UsuarioService usuarioService;
  private final AuthenticationManager authenticationManager;

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
  public void logout() {

  }

  @PostMapping("validate")
  public void validate() {

  }

  @PostMapping("forgot-password")
  public void forgotPassword() {

  }

  @PostMapping("reset-password")
  public void resetPassword() {

  }
}
