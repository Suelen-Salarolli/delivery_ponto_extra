package com.ufes.delivery.model.pagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Resultado de uma tentativa de pagamento simulado (US11).
 * Imutavel apos criacao — criado pelo PagamentoService.
 */
public class ResultadoPagamento {

    public enum Situacao { APROVADO, REPROVADO }

    private final int pedidoId;
    private final int pedidoCodigo;
    private final Situacao situacao;
    private final String formaPagamento;       // null se reprovado
    private final String identificadorTransacao; // null se reprovado
    private final BigDecimal valorPago;           // null se reprovado
    private final LocalDateTime prazoEstimadoEntrega; // null se reprovado
    private final LocalDateTime dataHoraTentativa;

    public ResultadoPagamento(int pedidoId, int pedidoCodigo, Situacao situacao,
                               String formaPagamento, String identificadorTransacao,
                               BigDecimal valorPago, LocalDateTime prazoEstimadoEntrega,
                               LocalDateTime dataHoraTentativa) {
        this.pedidoId = pedidoId;
        this.pedidoCodigo = pedidoCodigo;
        this.situacao = situacao;
        this.formaPagamento = formaPagamento;
        this.identificadorTransacao = identificadorTransacao;
        this.valorPago = valorPago;
        this.prazoEstimadoEntrega = prazoEstimadoEntrega;
        this.dataHoraTentativa = dataHoraTentativa;
    }

    public boolean isAprovado() { return situacao == Situacao.APROVADO; }

    public int getPedidoId() { return pedidoId; }
    public int getPedidoCodigo() { return pedidoCodigo; }
    public Situacao getSituacao() { return situacao; }
    public String getFormaPagamento() { return formaPagamento; }
    public String getIdentificadorTransacao() { return identificadorTransacao; }
    public BigDecimal getValorPago() { return valorPago; }
    public LocalDateTime getPrazoEstimadoEntrega() { return prazoEstimadoEntrega; }
    public LocalDateTime getDataHoraTentativa() { return dataHoraTentativa; }
}
