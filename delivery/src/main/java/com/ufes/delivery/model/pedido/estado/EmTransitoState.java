package com.ufes.delivery.model.pedido.estado;

import com.ufes.delivery.model.cadastro.EstadoPedido;

/** Pedido em transito para o cliente. */
public class EmTransitoState extends EstadoPedidoState {

    @Override
    public EstadoPedido valor() {
        return EstadoPedido.EM_TRANSITO;
    }
}
