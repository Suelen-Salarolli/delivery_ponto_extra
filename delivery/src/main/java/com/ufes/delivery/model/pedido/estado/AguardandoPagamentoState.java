package com.ufes.delivery.model.pedido.estado;

import com.ufes.delivery.model.cadastro.EstadoPedido;

/** Pedido aguardando pagamento. A aprovacao o leva para Aguardando entrega. */
public class AguardandoPagamentoState extends EstadoPedidoState {

    @Override
    public EstadoPedido valor() {
        return EstadoPedido.AGUARDANDO_PAGAMENTO;
    }

    @Override
    public EstadoPedidoState aprovarPagamento() {
        return new AguardandoEntregaState();
    }
}
