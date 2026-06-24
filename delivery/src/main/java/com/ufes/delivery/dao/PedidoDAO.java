package com.ufes.delivery.dao;

import com.ufes.delivery.db.ConexaoDB;
import com.ufes.delivery.model.pedido.PedidoCadastro;
import com.ufes.delivery.model.pedido.PedidoItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PedidoDAO {

    public int proximoCodigo() throws SQLException {
        String sql = "SELECT COALESCE(MAX(codigo), 1000) + 1 FROM pedidos";
        try (Connection conn = ConexaoDB.getConexao();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 1001;
    }

    public void salvar(PedidoCadastro pedido) throws SQLException {
        try (Connection conn = ConexaoDB.getConexao()) {
            conn.setAutoCommit(false);
            try {
                inserirPedido(conn, pedido);
                inserirItens(conn, pedido);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void inserirPedido(Connection conn, PedidoCadastro pedido) throws SQLException {
        String sql = """
            INSERT INTO pedidos
                (codigo, cliente_id, cliente_nome, endereco_id, cupom_codigo,
                 desconto_itens, desconto_entrega, taxa_entrega, valor_total,
                 data_pedido, estado)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pedido.getCodigo());
            ps.setInt(2, pedido.getCliente().getId());
            ps.setString(3, pedido.getCliente().getNome());
            ps.setInt(4, pedido.getEndereco().getId());
            ps.setString(5, pedido.getCupomCodigo());
            ps.setString(6, pedido.getDescontoItens().toPlainString());
            ps.setString(7, pedido.getDescontoEntrega().toPlainString());
            ps.setString(8, pedido.getTaxaEntrega().toPlainString());
            ps.setString(9, pedido.getValorTotal().toPlainString());
            ps.setString(10, pedido.getDataPedido().toString());
            ps.setString(11, pedido.getEstado().getDescricao());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    pedido.setId(rs.getInt(1));
                }
            }
        }
    }

    private void inserirItens(Connection conn, PedidoCadastro pedido) throws SQLException {
        String sql = """
            INSERT INTO pedido_itens
                (pedido_id, produto_id, produto_nome, categoria, quantidade, preco_unitario)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (PedidoItem item : pedido.getItens()) {
                ps.setInt(1, pedido.getId());
                ps.setInt(2, item.getProdutoId());
                ps.setString(3, item.getProdutoNome());
                ps.setString(4, item.getCategoria().getDescricao());
                ps.setInt(5, item.getQuantidade());
                ps.setString(6, item.getPrecoUnitario().toPlainString());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
