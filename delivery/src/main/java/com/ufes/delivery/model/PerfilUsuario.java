package com.ufes.delivery.model;

public enum PerfilUsuario {
    ADMINISTRADOR("Administrador"),
    ATENDENTE("Atendente");

    private final String descricao;

    PerfilUsuario(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static PerfilUsuario fromDescricao(String descricao) {
        for (PerfilUsuario p : values()) {
            if (p.descricao.equalsIgnoreCase(descricao)) return p;
        }
        throw new IllegalArgumentException("Perfil invalido: " + descricao);
    }

    @Override
    public String toString() {
        return descricao;
    }
}
