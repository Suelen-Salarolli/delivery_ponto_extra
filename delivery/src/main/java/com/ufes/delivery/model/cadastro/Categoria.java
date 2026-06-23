package com.ufes.delivery.model.cadastro;

/**
 * Lista controlada de categorias de produto (US07).
 * A categoria do cadastro deve ser selecionada exclusivamente entre estes valores.
 */
public enum Categoria {
    ALIMENTACAO("Alimentacao"),
    EDUCACAO("Educacao"),
    LAZER("Lazer"),
    ENTRETENIMENTO("Entretenimento"),
    SAUDE("Saude"),
    VESTUARIO("Vestuario"),
    OUTROS("Outros");

    private final String descricao;

    Categoria(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static Categoria fromDescricao(String descricao) {
        for (Categoria c : values()) {
            if (c.descricao.equalsIgnoreCase(descricao)) return c;
        }
        throw new IllegalArgumentException("Categoria invalida: " + descricao);
    }

    @Override
    public String toString() {
        return descricao;
    }
}
