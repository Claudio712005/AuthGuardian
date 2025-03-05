package com.clau.authguardian.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.clau.authguardian.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class JwtService {

  @Value("${api.security.token.secret}")
  private String secret;

  @Value("${api.security.token.accessExpirationHours}")
  private long accessExpirationHours;

  @Value("${api.security.token.refreshExpirationDays}")
  private long refreshExpirationDays;

  public String generateAccessToken(Usuario usuario) {
    return generateToken(usuario, accessExpirationHours, "access");
  }

  public String generateRefreshToken(Usuario usuario) {
    return generateToken(usuario, refreshExpirationDays * 24, "refresh");
  }

  private String generateToken(Usuario usuario, long expirationHours, String tokenType) {
    try {
      Algorithm algorithm = Algorithm.HMAC256(secret);
      return JWT.create()
              .withIssuer("auth-api")
              .withSubject(usuario.getUsername())
              .withExpiresAt(genExpirationDate(expirationHours))
              .withIssuedAt(Instant.now())
              .withClaim("jti", UUID.randomUUID().toString())
              .withClaim("tokenType", tokenType)
              .withClaim("roles", usuario.getRolesAsString())
              .withClaim("perfis", usuario.getPerfisAsString())
              .sign(algorithm);
    } catch (JWTCreationException e) {
      throw new RuntimeException("Erro na geração do token", e);
    }
  }

  public String validateToken(String token) {
    try {
      Algorithm algorithm = Algorithm.HMAC256(secret);
      return JWT.require(algorithm)
              .withIssuer("auth-api")
              .build()
              .verify(token)
              .getSubject();
    } catch (JWTVerificationException e) {
      return "";
    }
  }

  public String validateRefreshToken(String token) {
    try {
      Algorithm algorithm = Algorithm.HMAC256(secret);
      var decodedJWT = JWT.require(algorithm)
              .withIssuer("auth-api")
              .withClaim("tokenType", "refresh")
              .build()
              .verify(token);

      if(!decodedJWT.getExpiresAt().toInstant().isAfter(Instant.now())) {
        return "";
      }

      return decodedJWT.getSubject();
    } catch (JWTVerificationException e) {
      return "";
    }
  }

  private Instant genExpirationDate(long hours) {
    return LocalDateTime.now().plusHours(hours).toInstant(ZoneOffset.of("-03:00"));
  }
}
