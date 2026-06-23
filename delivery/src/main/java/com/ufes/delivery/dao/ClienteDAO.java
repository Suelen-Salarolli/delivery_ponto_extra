package com.ufes.delivery.dao;

import com.ufes.delivery.db.ConexaoDB;
import com.ufes.delivery.model.cadastro.Cliente;
import com.ufes.delivery.model.cadastro.Endereco;
import com.ufes.delivery.model.cadastro.Uf;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClienteDAO {

    public void salvar(Cliente cliente) throws SQLException {
        try (Connection conn = ConexaoDB.getConexao()) {
            conn.setAutoCommit(false);
            try {
                String sqlCliente = "INSERT INTO clientes (nome, cpf) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlCliente, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, cliente.getNome());
                    ps.setString(2, cliente.getCpf());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) cliente.setId(rs.getInt(1));
                    }
                }
                inserirEnderecos(conn, cliente);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void atualizar(Cliente cliente) throws SQLException {
        try (Connection conn = ConexaoDB.getConexao()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE clientes SET nome=?, cpf=? WHERE id=?")) {
                    ps.setString(1, cliente.getNome());
                    ps.setString(2, cliente.getCpf());
                    ps.setInt(3, cliente.getId());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM enderecos WHERE cliente_id=?")) {
                    ps.setInt(1, cliente.getId());
                    ps.executeUpdate();
                }
                inserirEnderecos(conn, cliente);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void excluir(int clienteId) throws SQLException {
        try (Connection conn = ConexaoDB.getConexao()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM enderecos WHERE cliente_id=?")) {
                    ps.setInt(1, clienteId); ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM clientes WHERE id=?")) {
                    ps.setInt(1, clienteId); ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) { conn.rollback(); throw e; }
        }
    }

    private void inserirEnderecos(Connection conn, Cliente cliente) throws SQLException {
        String sql = """
            INSERT INTO enderecos
                (cliente_id, logradouro, numero, complemento, bairro, cidade, uf, cep, padrao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (Endereco e : cliente.getEnderecos()) {
                ps.setInt(1, cliente.getId());
                ps.setString(2, e.getLogradouro());
                ps.setString(3, e.getNumero());
                ps.setString(4, e.getComplemento());
                ps.setString(5, e.getBairro());
                ps.setString(6, e.getCidade());
                ps.setString(7, e.getUf().name());
                ps.setString(8, e.getCep());
                ps.setInt(9, e.isPadrao() ? 1 : 0);
                ps.addBatch();
            }
            ps.executeBatch();
            // Atualiza IDs dos endereços
            try (ResultSet rs = ps.getGeneratedKeys()) {
                int i = 0;
                List<Endereco> lista = new ArrayList<>(cliente.getEnderecos());
                while (rs.next() && i < lista.size()) {
                    lista.get(i++).setId(rs.getInt(1));
                }
            }
        }
    }

    public boolean existeCpf(String cpfNormalizado) throws SQLException {
        return existeCpf(cpfNormalizado, 0);
    }

    public boolean existeCpf(String cpfNormalizado, int idExcluido) throws SQLException {
        String sql = "SELECT COUNT(*) FROM clientes WHERE cpf = ? AND id <> ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cpfNormalizado);
            ps.setInt(2, idExcluido);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public Optional<Cliente> buscarPorId(int id) throws SQLException {
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM clientes WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Cliente c = mapearCliente(rs);
                    carregarEnderecos(c);
                    return Optional.of(c);
                }
            }
        }
        return Optional.empty();
    }

    public List<Cliente> buscarPorNome(String filtro) throws SQLException {
        String sql = "SELECT * FROM clientes WHERE LOWER(nome) LIKE LOWER(?) ORDER BY nome";
        List<Cliente> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + filtro + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearCliente(rs));
            }
        }
        return lista;
    }

    public List<Cliente> buscarPorCpf(String cpfNormalizado) throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM clientes WHERE cpf = ? ORDER BY nome")) {
            ps.setString(1, cpfNormalizado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearCliente(rs));
            }
        }
        return lista;
    }

    public List<Cliente> listarTodos() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConexao();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM clientes ORDER BY nome")) {
            while (rs.next()) lista.add(mapearCliente(rs));
        }
        return lista;
    }

    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("id"));
        c.setNome(rs.getString("nome"));
        c.setCpf(rs.getString("cpf"));
        return c;
    }

    public void carregarEnderecos(Cliente cliente) throws SQLException {
        String sql = "SELECT * FROM enderecos WHERE cliente_id = ? ORDER BY id";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cliente.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Endereco e = new Endereco();
                    e.setId(rs.getInt("id"));
                    e.setLogradouro(rs.getString("logradouro"));
                    e.setNumero(rs.getString("numero"));
                    e.setComplemento(rs.getString("complemento"));
                    e.setBairro(rs.getString("bairro"));
                    e.setCidade(rs.getString("cidade"));
                    e.setUf(Uf.de(rs.getString("uf")));
                    e.setCep(rs.getString("cep"));
                    e.setPadrao(rs.getInt("padrao") == 1);
                    cliente.adicionarEndereco(e);
                }
            }
        }
    }
}
