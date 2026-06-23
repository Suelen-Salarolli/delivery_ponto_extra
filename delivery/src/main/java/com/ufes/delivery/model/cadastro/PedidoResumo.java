package com.ufes.delivery.model.cadastro;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Projecao de pedido usada pelo painel operacional (US04).
 *
 * AIDEV-NOTE: read model deliberadamente enxuto para o painel da Fase 2. O
 * agregado Pedido completo (itens, cupom, pagamento, transicoes) e construido
 * na Fase 3/4 (US09/US10) sobre a mesma tabela 'pedidos', estendendo este resumo.
 */
public class PedidoResumo {

    private int codigo;
    private String clienteNome;
    private LocalDate dataPedido;
    private LocalDate dataConclusao; // nula enquanto nao Entregue
    private EstadoPedido estado;
    private BigDecimal valorTotal;

    public PedidoResumo() {}

    public PedidoResumo(int codigo, String clienteNome, LocalDate dataPedido,
                        LocalDate dataConclusao, EstadoPedido estado, BigDecimal valorTotal) {
        this.codigo = codigo;
        this.clienteNome = clienteNome;
        this.dataPedido = dataPedido;
        this.dataConclusao = dataConclusao;
        this.estado = estado;
        this.valorTotal = valorTotal;
    }

    public int getCodigo() { return codigo; }
    public String getClienteNome() { return clienteNome; }
    public LocalDate getDataPedido() { return dataPedido; }
    public LocalDate getDataConclusao() { return dataConclusao; }
    public EstadoPedido getEstado() { return estado; }
    public BigDecimal getValorTotal() { return valorTotal; }
}
