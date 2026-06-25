package com.ufes.delivery.desconto.entrega;

import com.ufes.delivery.model.pedido.PedidoCadastro;
import java.math.BigDecimal;

/**
 * Desconto na taxa de entrega quando o subtotal dos itens ultrapassa um limite.
 */
public class DescontoEntregaPorValorPedido implements DescontoEntregaStrategy {

    private static final BigDecimal LIMITE_VALOR_PEDIDO = new BigDecimal("200.00");
    private static final BigDecimal VALOR_DESCONTO = new BigDecimal("5.00");

    @Override
    public boolean seAplica(PedidoCadastro pedido) {
        return pedido.getSubtotalItens().compareTo(LIMITE_VALOR_PEDIDO) > 0;
    }

    @Override
    public BigDecimal calcular(PedidoCadastro pedido) {
        return seAplica(pedido) ? VALOR_DESCONTO : BigDecimal.ZERO;
    }

    @Override
    public String nome() {
        return "Desconto entrega por valor do pedido";
    }
}
