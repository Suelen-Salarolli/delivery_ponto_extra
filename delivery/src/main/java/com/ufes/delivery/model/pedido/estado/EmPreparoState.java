package com.ufes.delivery.model.pedido.estado;

import com.ufes.delivery.model.cadastro.EstadoPedido;

/** Pedido em preparo. Nao aceita nova aprovacao de pagamento. */
public class EmPreparoState extends EstadoPedidoState {

    @Override
    public EstadoPedido valor() {
        return EstadoPedido.EM_PREPARO;
    }
}
