package com.ufes.delivery.dao;

import com.ufes.delivery.db.ConexaoDB;
import com.ufes.delivery.model.cadastro.MovimentacaoEstoque;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Persistencia de movimentacoes de estoque (US08).
 * Cada confirmacao atualiza o estoque_atual do produto e registra a movimentacao
 * em transacao unica — garantia da regra transversal de persistencia.
 */
public class MovimentacaoEstoqueDAO {

    private static final DateTimeFormatter FMT_DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Confirma a movimentacao: atualiza estoque_atual do produto e insere o registro,
     * tudo em uma unica transacao.
     */
    public void confirmar(MovimentacaoEstoque mov) throws SQLException {
        try (Connection conn = ConexaoDB.getConexao()) {
            conn.setAutoCommit(false);
            try {
                // 1. Atualiza estoque do produto
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE produtos SET estoque_atual = ? WHERE id = ?")) {
                    ps.setInt(1, mov.getEstoquePosterior());
                    ps.setInt(2, mov.getProdutoId());
                    ps.executeUpdate();
                }

                // 2. Registra movimentacao
                String sql = """
                    INSERT INTO estoque_movimentacoes
                        (produto_id, tipo, quantidade, estoque_anterior, estoque_posterior,
                         data_movimentacao, motivo, nota_fiscal, usuario, data_hora_registro)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, mov.getProdutoId());
                    ps.setString(2, mov.getTipo());
                    ps.setInt(3, mov.getQuantidade());
                    ps.setInt(4, mov.getEstoqueAnterior());
                    ps.setInt(5, mov.getEstoquePosterior());
                    ps.setString(6, mov.getDataMovimentacao().toString()); // ISO
                    ps.setString(7, mov.getMotivo());
                    ps.setString(8, mov.getNotaFiscal());
                    ps.setString(9, mov.getUsuario());
                    ps.setString(10, mov.getDataHoraRegistro().format(FMT_DT));
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) mov.setId(rs.getInt(1));
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /** Consulta o estoque atual diretamente do banco (evita leitura desatualizada). */
    public int estoqueAtual(int produtoId) throws SQLException {
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT estoque_atual FROM produtos WHERE id = ?")) {
            ps.setInt(1, produtoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Produto nao encontrado: id=" + produtoId);
    }
}
