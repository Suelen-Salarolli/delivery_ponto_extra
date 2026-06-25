package com.ufes.delivery.model.pedido.estado;

import com.ufes.delivery.model.cadastro.EstadoPedido;

/** Estado terminal: pedido entregue. Nenhuma transicao adicional e permitida. */
public class EntregueState extends EstadoPedidoState {

    @Override
    public EstadoPedido valor() {
        return EstadoPedido.ENTREGUE;
    }
}
