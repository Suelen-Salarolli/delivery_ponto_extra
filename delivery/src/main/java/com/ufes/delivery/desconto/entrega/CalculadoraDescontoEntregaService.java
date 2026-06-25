package com.ufes.delivery.desconto.entrega;

import com.ufes.delivery.model.pedido.PedidoCadastro;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Contexto do padrao Strategy: agrega as formas de desconto de entrega e calcula
 * o desconto total aplicavel, limitado a taxa base (a entrega nunca fica negativa).
 *
 * SOLID:
 *  - OCP: novas formas de desconto entram pela lista, sem alterar esta classe.
 *  - DIP: depende da abstracao DescontoEntregaStrategy, nao das implementacoes.
 */
public class CalculadoraDescontoEntregaService {

    private final List<DescontoEntregaStrategy> estrategias;

    public CalculadoraDescontoEntregaService() {
        this(List.of(
            new DescontoEntregaPorBairro(),
            new DescontoEntregaPorCategoria(),
            new DescontoEntregaPorValorPedido()
        ));
    }

    /** Construtor para testes/extensao: injeta a lista de estrategias. */
    public CalculadoraDescontoEntregaService(List<DescontoEntregaStrategy> estrategias) {
        this.estrategias = List.copyOf(estrategias);
    }

    /**
     * Soma dos descontos aplicaveis, limitada a taxaBase. Estrategias sao
     * consideradas em ordem ate esgotar a taxa disponivel.
     */
    public BigDecimal calcularDescontoEntrega(PedidoCadastro pedido, BigDecimal taxaBase) {
        BigDecimal total = BigDecimal.ZERO;
        for (DescontoEntregaStrategy estrategia : estrategias) {
            if (total.compareTo(taxaBase) >= 0) {
                break;
            }
            if (estrategia.seAplica(pedido)) {
                BigDecimal restante = taxaBase.subtract(total);
                BigDecimal desconto = estrategia.calcular(pedido).min(restante);
                if (desconto.signum() > 0) {
                    total = total.add(desconto);
                }
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
