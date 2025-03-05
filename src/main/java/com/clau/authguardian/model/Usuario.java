package com.clau.authguardian.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "usuario")
@EntityListeners(AuditingEntityListener.class)
public class Usuario implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "nome", nullable = false)
  private String nome;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "senha", nullable = false)
  private String senha;

  @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<UsuarioPerfil> perfis;

  @UpdateTimestamp
  @Column(name = "data_atualizacao")
  private LocalDateTime dataAtualizacao;

  @CreationTimestamp
  @Column(name = "data_criacao")
  private LocalDateTime dataCriacao;

  @CreatedBy
  @Column(name = "usuario_criacao")
  private String usuarioCriacao;

  @LastModifiedBy
  @Column(name = "usuario_atualizacao")
  private String usuarioAtualizacao;

  public Set<Perfil> getPerfis() {
    return perfis.stream()
            .map(UsuarioPerfil::getPerfil)
            .collect(Collectors.toSet());
  }

  public List<String> getPerfisAsString() {
    return perfis.stream()
            .map(UsuarioPerfil::getPerfil)
            .map(Perfil::getNome)
            .collect(Collectors.toList());
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return perfis.stream()
            .map(UsuarioPerfil::getPerfil)
            .flatMap(perfil -> perfil.getRoles().stream())
            .map(PerfilRole::getRole)
            .collect(Collectors.toSet());
  }

  public List<String> getRolesAsString() {
    return perfis.stream()
            .map(UsuarioPerfil::getPerfil)
            .flatMap(perfil -> perfil.getRoles().stream())
            .map(PerfilRole::getRole)
            .map(Role::getNome)
            .collect(Collectors.toList());
  }

  @Override
  public String getPassword() {
    return this.senha;
  }

  @Override
  public String getUsername() {
    return this.email;
  }

  public Usuario() {
  }

  public Usuario(Long id, String nome, String email, String senha, Set<UsuarioPerfil> perfis, LocalDateTime dataAtualizacao, LocalDateTime dataCriacao, String usuarioCriacao, String usuarioAtualizacao) {
    this.id = id;
    this.nome = nome;
    this.email = email;
    this.senha = senha;
    this.perfis = perfis;
    this.dataAtualizacao = dataAtualizacao;
    this.dataCriacao = dataCriacao;
    this.usuarioCriacao = usuarioCriacao;
    this.usuarioAtualizacao = usuarioAtualizacao;
  }

  public Usuario(Long id, String nome, String email, String senha) {
    this.id = id;
    this.nome = nome;
    this.email = email;
    this.senha = senha;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getSenha() {
    return senha;
  }

  public void setSenha(String senha) {
    this.senha = senha;
  }

  public void setPerfis(Set<UsuarioPerfil> perfis) {
    this.perfis = perfis;
  }

  public LocalDateTime getDataAtualizacao() {
    return dataAtualizacao;
  }

  public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
    this.dataAtualizacao = dataAtualizacao;
  }

  public LocalDateTime getDataCriacao() {
    return dataCriacao;
  }

  public void setDataCriacao(LocalDateTime dataCriacao) {
    this.dataCriacao = dataCriacao;
  }

  public String getUsuarioCriacao() {
    return usuarioCriacao;
  }

  public void setUsuarioCriacao(String usuarioCriacao) {
    this.usuarioCriacao = usuarioCriacao;
  }

  public String getUsuarioAtualizacao() {
    return usuarioAtualizacao;
  }

  public void setUsuarioAtualizacao(String usuarioAtualizacao) {
    this.usuarioAtualizacao = usuarioAtualizacao;
  }
}
