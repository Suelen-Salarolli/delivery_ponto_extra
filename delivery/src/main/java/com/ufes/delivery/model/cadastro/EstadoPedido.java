package com.ufes.delivery.model.cadastro;

/**
 * Dominio de estados do pedido (regra transversal "Estados do pedido").
 * A data de conclusao so e preenchida para pedido Entregue.
 */
public enum EstadoPedido {
    NOVO("Novo"),
    AGUARDANDO_PAGAMENTO("Aguardando pagamento"),
    EM_PREPARO("Em preparo"),
    AGUARDANDO_ENTREGA("Aguardando entrega"),
    EM_TRANSITO("Em transito"),
    ENTREGUE("Entregue");

    private final String descricao;

    EstadoPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static EstadoPedido fromDescricao(String descricao) {
        for (EstadoPedido e : values()) {
            if (e.descricao.equalsIgnoreCase(descricao)) return e;
        }
        throw new IllegalArgumentException("Estado de pedido invalido: " + descricao);
    }

    @Override
    public String toString() {
        return descricao;
    }
}
