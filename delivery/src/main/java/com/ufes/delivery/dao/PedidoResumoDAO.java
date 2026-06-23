package com.ufes.delivery.dao;

import com.ufes.delivery.db.ConexaoDB;
import com.ufes.delivery.model.cadastro.EstadoPedido;
import com.ufes.delivery.model.cadastro.PedidoResumo;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso ao read model de pedidos do painel (US04).
 * Datas persistidas em ISO (yyyy-MM-dd). Adaptado ao schema completo da Fase 3.
 */
public class PedidoResumoDAO {

    public List<PedidoResumo> listarPorDataPedido(LocalDate data) throws SQLException {
        String sql = "SELECT * FROM pedidos WHERE data_pedido = ? ORDER BY codigo";
        List<PedidoResumo> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, data.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public int contarEntreguesPorConclusao(LocalDate dataOperacao) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pedidos WHERE estado = ? AND data_conclusao = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, EstadoPedido.ENTREGUE.getDescricao());
            ps.setString(2, dataOperacao.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public boolean existeAlgum() throws SQLException {
        try (Connection conn = ConexaoDB.getConexao();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM pedidos")) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private PedidoResumo mapear(ResultSet rs) throws SQLException {
        String conclusaoTxt = rs.getString("data_conclusao");
        return new PedidoResumo(
            rs.getInt("codigo"),
            rs.getString("cliente_nome"),
            LocalDate.parse(rs.getString("data_pedido")),
            conclusaoTxt == null ? null : LocalDate.parse(conclusaoTxt),
            EstadoPedido.fromDescricao(rs.getString("estado")),
            new BigDecimal(rs.getString("valor_total"))
        );
    }
}
