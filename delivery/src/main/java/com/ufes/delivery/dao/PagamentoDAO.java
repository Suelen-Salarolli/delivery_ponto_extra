package com.ufes.delivery.dao;

import com.ufes.delivery.db.ConexaoDB;
import com.ufes.delivery.model.pagamento.ResultadoPagamento;
import com.ufes.delivery.model.cadastro.EstadoPedido;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

/**
 * Persiste tentativas de pagamento e executa a baixa de estoque + mudanca de
 * estado do pedido em transacao unica (US10/US11).
 *
 * AIDEV-NOTE: a transacao cobre 3 operacoes atomicas:
 *   1. INSERT em pagamentos
 *   2. UPDATE de estado do pedido para Aguardando entrega
 *   3. UPDATE de estoque_atual para cada item do pedido
 * Se qualquer passo falhar, rollback total — nenhuma alteracao persiste.
 */
public class PagamentoDAO {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Persiste resultado aprovado: registra pagamento, baixa estoque de cada item
     * e muda estado do pedido para Aguardando entrega — tudo atomico.
     */
    public void confirmarAprovado(ResultadoPagamento resultado,
                                   java.util.List<com.ufes.delivery.model.pedido.PedidoItem> itens,
                                   EstadoPedido novoEstado)
            throws SQLException {

        try (Connection conn = ConexaoDB.getConexao()) {
            conn.setAutoCommit(false);
            try {
                // 1. Registra pagamento
                inserirPagamento(conn, resultado);

                // 2. Baixa estoque de cada item
                for (com.ufes.delivery.model.pedido.PedidoItem item : itens) {
                    baixarEstoque(conn, item.getProdutoId(), item.getQuantidade());
                }

                // 3. Persiste o novo estado definido pelo padrao State
                atualizarEstadoPedido(conn, resultado.getPedidoId(), novoEstado);

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Persiste resultado reprovado: apenas registra a tentativa.
     * Estoque e estado do pedido permanecem inalterados.
     */
    public void registrarReprovado(ResultadoPagamento resultado) throws SQLException {
        try (Connection conn = ConexaoDB.getConexao()) {
            inserirPagamento(conn, resultado);
        }
    }

    private void inserirPagamento(Connection conn, ResultadoPagamento r) throws SQLException {
        String sql = """
            INSERT INTO pagamentos
                (pedido_id, forma_pagamento, resultado, identificador_transacao,
                 valor_pago, prazo_estimado_entrega, data_hora_tentativa)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getPedidoId());
            ps.setString(2, r.getFormaPagamento() != null ? r.getFormaPagamento() : "");
            ps.setString(3, r.getSituacao().name());
            ps.setString(4, r.getIdentificadorTransacao());
            ps.setString(5, r.getValorPago() != null ? r.getValorPago().toPlainString() : null);
            ps.setString(6, r.getPrazoEstimadoEntrega() != null
                ? r.getPrazoEstimadoEntrega().format(FMT) : null);
            ps.setString(7, r.getDataHoraTentativa().format(FMT));
            ps.executeUpdate();
        }
    }

    private void baixarEstoque(Connection conn, int produtoId, int quantidade) throws SQLException {
        String sql = """
            UPDATE produtos
               SET estoque_atual = estoque_atual - ?
             WHERE id = ? AND estoque_atual >= ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantidade);
            ps.setInt(2, produtoId);
            ps.setInt(3, quantidade);
            int linhas = ps.executeUpdate();
            if (linhas == 0) {
                throw new SQLException(
                    "Estoque insuficiente para o produto id=" + produtoId
                    + " no momento da confirmacao");
            }
        }
    }

    private void atualizarEstadoPedido(Connection conn, int pedidoId,
                                        EstadoPedido estado) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE pedidos SET estado = ? WHERE id = ?")) {
            ps.setString(1, estado.getDescricao());
            ps.setInt(2, pedidoId);
            ps.executeUpdate();
        }
    }

    /** Verifica disponibilidade de estoque para todos os itens antes de processar. */
    public java.util.List<String> verificarDisponibilidade(
            java.util.List<com.ufes.delivery.model.pedido.PedidoItem> itens)
            throws SQLException {

        java.util.List<String> insuficientes = new java.util.ArrayList<>();
        String sql = "SELECT nome, estoque_atual FROM produtos WHERE id = ?";

        try (Connection conn = ConexaoDB.getConexao()) {
            for (com.ufes.delivery.model.pedido.PedidoItem item : itens) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, item.getProdutoId());
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            int disponivel = rs.getInt("estoque_atual");
                            if (disponivel < item.getQuantidade()) {
                                insuficientes.add(String.format(
                                    "%s: solicitado %d, disponivel %d",
                                    rs.getString("nome"),
                                    item.getQuantidade(), disponivel));
                            }
                        }
                    }
                }
            }
        }
        return insuficientes;
    }
}
