package com.clau.authguardian.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@Entity
@Table(name = "perfil")
@EntityListeners(AuditingEntityListener.class)
public class Perfil {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "nome", nullable = false, unique = true)
  private String nome;

  @Column(name = "descricao")
  private String descricao;

  @OneToMany(mappedBy = "perfil")
  private List<UsuarioPerfil> usuarios;

  @OneToMany(mappedBy = "perfil", fetch = FetchType.EAGER)
  private List<PerfilRole> roles;

  @CreatedBy
  @Column(name = "usuario_criacao")
  private String usuarioCriacao;

  @LastModifiedBy
  @Column(name = "usuario_atualizacao")
  private String usuarioAtualizacao;

  public Perfil() {
  }

  public Perfil(Long id, String nome, String descricao) {
    this.id = id;
    this.nome = nome;
    this.descricao = descricao;
  }

  public Perfil(Long id, String nome, String descricao, List<UsuarioPerfil> usuarios, List<PerfilRole> roles, String usuarioCriacao, String usuarioAtualizacao) {
    this.id = id;
    this.nome = nome;
    this.descricao = descricao;
    this.usuarios = usuarios;
    this.roles = roles;
    this.usuarioCriacao = usuarioCriacao;
    this.usuarioAtualizacao = usuarioAtualizacao;
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

  public String getDescricao() {
    return descricao;
  }

  public void setDescricao(String descricao) {
    this.descricao = descricao;
  }

  public List<UsuarioPerfil> getUsuarios() {
    return usuarios;
  }

  public void setUsuarios(List<UsuarioPerfil> usuarios) {
    this.usuarios = usuarios;
  }

  public List<PerfilRole> getRoles() {
    return roles;
  }

  public void setRoles(List<PerfilRole> roles) {
    this.roles = roles;
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
