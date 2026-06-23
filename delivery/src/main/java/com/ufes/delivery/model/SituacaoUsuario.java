package com.ufes.delivery.model;

public enum SituacaoUsuario {
    AUTORIZADO("Autorizado"),
    PENDENTE("Pendente"),
    NAO_AUTORIZADO("Nao autorizado");

    private final String descricao;

    SituacaoUsuario(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static SituacaoUsuario fromDescricao(String descricao) {
        for (SituacaoUsuario s : values()) {
            if (s.descricao.equalsIgnoreCase(descricao)) return s;
        }
        throw new IllegalArgumentException("Situacao invalida: " + descricao);
    }

    @Override
    public String toString() {
        return descricao;
    }
}
