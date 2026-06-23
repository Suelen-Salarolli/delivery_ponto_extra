package com.ufes.delivery.service;

import com.ufes.delivery.auditoria.IAuditoriaService;
import com.ufes.delivery.dao.UsuarioDAO;
import com.ufes.delivery.model.*;
import com.ufes.delivery.util.SenhaUtil;
import java.sql.SQLException;
import java.util.List;

public class UsuarioService {

    private final UsuarioDAO usuarioDAO;
    private final IAuditoriaService auditoria;

    public UsuarioService(UsuarioDAO usuarioDAO, IAuditoriaService auditoria) {
        this.usuarioDAO = usuarioDAO;
        this.auditoria = auditoria;
    }

    /** US02 — cadastro de usuário */
    public Usuario cadastrar(String nome, String username, String senha) throws SQLException {
        // Validações
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setUsername(username);

        SenhaUtil.validarTamanho(senha);

        if (usuarioDAO.existeUsername(username)) {
            throw new IllegalArgumentException("Nome de usuario ja esta em uso");
        }

        u.setSenhaHash(SenhaUtil.hash(senha));

        // Primeiro usuário → Administrador/Autorizado; demais → Atendente/Pendente
        if (!usuarioDAO.existeAdministrador()) {
            u.setPerfil(PerfilUsuario.ADMINISTRADOR);
            u.setSituacao(SituacaoUsuario.AUTORIZADO);
        } else {
            u.setPerfil(PerfilUsuario.ATENDENTE);
            u.setSituacao(SituacaoUsuario.PENDENTE);
        }

        usuarioDAO.salvar(u);
        auditoria.registrar(Sessao.getInstance().getUsernameAtual(),
            "CADASTRO_USUARIO", "usuario:" + username,
            "Sucesso", "Perfil: " + u.getPerfil().getDescricao());
        return u;
    }

    /** US03 — autorizar lista de usuários */
    public void autorizar(List<Usuario> usuarios) throws SQLException {
        for (Usuario u : usuarios) {
            u.setSituacao(SituacaoUsuario.AUTORIZADO);
            usuarioDAO.atualizar(u);
            auditoria.registrar(Sessao.getInstance().getUsernameAtual(),
                "AUTORIZACAO_USUARIO", "usuario:" + u.getUsername(),
                "Autorizado", null);
        }
    }

    /** US03 — desautorizar lista de usuários */
    public void desautorizar(List<Usuario> usuarios) throws SQLException {
        for (Usuario u : usuarios) {
            u.setSituacao(SituacaoUsuario.NAO_AUTORIZADO);
            usuarioDAO.atualizar(u);
            auditoria.registrar(Sessao.getInstance().getUsernameAtual(),
                "DESAUTORIZACAO_USUARIO", "usuario:" + u.getUsername(),
                "Nao autorizado", null);
        }
    }

    /** US03 — excluir lista de usuários */
    public void excluir(List<Usuario> usuarios) throws SQLException {
        for (Usuario u : usuarios) {
            usuarioDAO.excluir(u.getId());
            auditoria.registrar(Sessao.getInstance().getUsernameAtual(),
                "EXCLUSAO_USUARIO", "usuario:" + u.getUsername(),
                "Excluido", null);
        }
    }

    /** US03 — atualizar perfil de usuário */
    public void atualizarPerfil(Usuario usuario, PerfilUsuario novoPerfil) throws SQLException {
        usuario.setPerfil(novoPerfil);
        usuarioDAO.atualizar(usuario);
        auditoria.registrar(Sessao.getInstance().getUsernameAtual(),
            "ALTERACAO_PERFIL", "usuario:" + usuario.getUsername(),
            "Sucesso", "Novo perfil: " + novoPerfil.getDescricao());
    }

    public List<Usuario> buscarPorNome(String filtro) throws SQLException {
        if (filtro == null || filtro.isBlank()) return usuarioDAO.listarTodos();
        return usuarioDAO.buscarPorNome(filtro.trim());
    }

    public List<Usuario> listarTodos() throws SQLException {
        return usuarioDAO.listarTodos();
    }
}
