package com.ufes.delivery.model.pedido.estado;

import com.ufes.delivery.model.cadastro.EstadoPedido;

/** Estado inicial do pedido. Pode ter o pagamento aprovado. */
public class NovoState extends EstadoPedidoState {

    @Override
    public EstadoPedido valor() {
        return EstadoPedido.NOVO;
    }

    @Override
    public EstadoPedidoState aprovarPagamento() {
        return new AguardandoEntregaState();
    }
}
