package com.ufes.delivery.model.pedido.estado;

import com.ufes.delivery.model.cadastro.EstadoPedido;

/** Pedido pago e pronto para entrega. Pagamento ja foi aprovado. */
public class AguardandoEntregaState extends EstadoPedidoState {

    @Override
    public EstadoPedido valor() {
        return EstadoPedido.AGUARDANDO_ENTREGA;
    }
}
