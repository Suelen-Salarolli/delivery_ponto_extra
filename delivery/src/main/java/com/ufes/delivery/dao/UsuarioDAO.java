package com.ufes.delivery.dao;

import com.ufes.delivery.db.ConexaoDB;
import com.ufes.delivery.model.PerfilUsuario;
import com.ufes.delivery.model.SituacaoUsuario;
import com.ufes.delivery.model.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioDAO {

    public void salvar(Usuario usuario) throws SQLException {
        String sql = """
            INSERT INTO usuarios (nome, username, senha_hash, perfil, situacao)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getUsername());
            ps.setString(3, usuario.getSenhaHash());
            ps.setString(4, usuario.getPerfil().getDescricao());
            ps.setString(5, usuario.getSituacao().getDescricao());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) usuario.setId(rs.getInt(1));
            }
        }
    }

    public void atualizar(Usuario usuario) throws SQLException {
        String sql = """
            UPDATE usuarios SET nome=?, perfil=?, situacao=? WHERE id=?
        """;
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getPerfil().getDescricao());
            ps.setString(3, usuario.getSituacao().getDescricao());
            ps.setInt(4, usuario.getId());
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM usuarios WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Optional<Usuario> buscarPorUsername(String username) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE username = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
        }
        return Optional.empty();
    }

    public boolean existeAdministrador() throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE perfil = 'Administrador'";
        try (Connection conn = ConexaoDB.getConexao();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public boolean existeUsername(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public List<Usuario> buscarPorNome(String filtro) throws SQLException {
        String sql = """
            SELECT * FROM usuarios
            WHERE LOWER(nome) LIKE LOWER(?) OR LOWER(username) LIKE LOWER(?)
            ORDER BY nome
        """;
        List<Usuario> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + filtro + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConexao();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM usuarios ORDER BY nome")) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNome(rs.getString("nome"));
        u.setUsername(rs.getString("username"));
        u.setSenhaHash(rs.getString("senha_hash"));
        u.setPerfil(PerfilUsuario.fromDescricao(rs.getString("perfil")));
        u.setSituacao(SituacaoUsuario.fromDescricao(rs.getString("situacao")));
        return u;
    }
}
