package com.ufes.delivery.dao;

import com.ufes.delivery.db.ConexaoDB;
import com.ufes.delivery.model.cadastro.Categoria;
import com.ufes.delivery.model.cadastro.Produto;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProdutoDAO {

    public void salvar(Produto p) throws SQLException {
        String sql = """
            INSERT INTO produtos (codigo, nome, categoria, preco_unitario, estoque_atual)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getCodigo());
            ps.setString(2, p.getNome());
            ps.setString(3, p.getCategoria().getDescricao());
            ps.setString(4, p.getPrecoUnitario().toPlainString());
            ps.setInt(5, p.getEstoqueAtual());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) p.setId(rs.getInt(1));
            }
        }
    }

    public void atualizar(Produto p) throws SQLException {
        String sql = """
            UPDATE produtos SET nome=?, categoria=?, preco_unitario=?, estoque_atual=? WHERE id=?
        """;
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNome());
            ps.setString(2, p.getCategoria().getDescricao());
            ps.setString(3, p.getPrecoUnitario().toPlainString());
            ps.setInt(4, p.getEstoqueAtual());
            ps.setInt(5, p.getId());
            ps.executeUpdate();
        }
    }

    public boolean existeCodigo(int codigo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM produtos WHERE codigo = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public Optional<Produto> buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM produtos WHERE id = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
        }
        return Optional.empty();
    }

    public List<Produto> buscarPorNome(String filtro) throws SQLException {
        return buscarPorColuna("LOWER(nome) LIKE LOWER(?)", "%" + filtro + "%");
    }

    public List<Produto> buscarPorCategoria(String categoria) throws SQLException {
        return buscarPorColuna("categoria = ?", categoria);
    }

    public List<Produto> buscarPorCodigo(int codigo) throws SQLException {
        List<Produto> lista = new ArrayList<>();
        String sql = "SELECT * FROM produtos WHERE codigo = ? ORDER BY codigo";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Produto> listarTodos() throws SQLException {
        List<Produto> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConexao();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM produtos ORDER BY nome")) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    private List<Produto> buscarPorColuna(String condicao, String valor) throws SQLException {
        List<Produto> lista = new ArrayList<>();
        String sql = "SELECT * FROM produtos WHERE " + condicao + " ORDER BY nome";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Produto mapear(ResultSet rs) throws SQLException {
        Produto p = new Produto();
        p.setId(rs.getInt("id"));
        p.setCodigo(rs.getInt("codigo"));
        p.setNome(rs.getString("nome"));
        p.setCategoria(Categoria.fromDescricao(rs.getString("categoria")));
        p.setPrecoUnitario(new BigDecimal(rs.getString("preco_unitario")));
        p.setEstoqueAtual(rs.getInt("estoque_atual"));
        return p;
    }
}
