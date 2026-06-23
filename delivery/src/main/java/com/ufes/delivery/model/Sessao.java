package com.ufes.delivery.model;

import java.time.LocalDateTime;

public class Sessao {

    private static Sessao instancia;

    private Usuario usuario;
    private LocalDateTime dataHoraLogin;

    private Sessao() {}

    public static Sessao getInstance() {
        if (instancia == null) instancia = new Sessao();
        return instancia;
    }

    public void iniciar(Usuario usuario) {
        this.usuario = usuario;
        this.dataHoraLogin = LocalDateTime.now();
    }

    public void encerrar() {
        this.usuario = null;
        this.dataHoraLogin = null;
    }

    public boolean isAtiva() {
        return usuario != null;
    }

    public Usuario getUsuario() { return usuario; }
    public LocalDateTime getDataHoraLogin() { return dataHoraLogin; }

    public String getUsernameAtual() {
        return isAtiva() ? usuario.getUsername() : "sistema";
    }
}
