package com.ufes.delivery.model;

public class Usuario {

    private int id;
    private String nome;
    private String username;
    private String senhaHash;
    private PerfilUsuario perfil;
    private SituacaoUsuario situacao;

    public Usuario() {}

    public Usuario(String nome, String username, String senhaHash,
                   PerfilUsuario perfil, SituacaoUsuario situacao) {
        setNome(nome);
        setUsername(username);
        this.senhaHash = senhaHash;
        this.perfil = perfil;
        this.situacao = situacao;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome nao pode ser vazio");
        if (nome.trim().length() < 2 || nome.trim().length() > 120)
            throw new IllegalArgumentException("Nome deve ter entre 2 e 120 caracteres");
        this.nome = nome.trim();
    }

    public String getUsername() { return username; }
    public void setUsername(String username) {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Nome de usuario nao pode ser vazio");
        if (!username.matches("[a-z0-9]{3,30}"))
            throw new IllegalArgumentException(
                "Nome de usuario deve ter entre 3 e 30 caracteres, apenas letras minusculas e numeros, sem espacos");
        this.username = username;
    }

    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }

    public PerfilUsuario getPerfil() { return perfil; }
    public void setPerfil(PerfilUsuario perfil) { this.perfil = perfil; }

    public SituacaoUsuario getSituacao() { return situacao; }
    public void setSituacao(SituacaoUsuario situacao) { this.situacao = situacao; }

    public boolean isAutorizado() {
        return SituacaoUsuario.AUTORIZADO.equals(situacao);
    }

    public boolean isAdministrador() {
        return PerfilUsuario.ADMINISTRADOR.equals(perfil);
    }

    @Override
    public String toString() {
        return "Usuario{id=" + id + ", username='" + username + "', perfil=" + perfil + ", situacao=" + situacao + "}";
    }
}
