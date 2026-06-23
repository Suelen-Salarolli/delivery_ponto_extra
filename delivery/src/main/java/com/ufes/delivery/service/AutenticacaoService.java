package com.ufes.delivery.service;

import com.ufes.delivery.auditoria.IAuditoriaService;
import com.ufes.delivery.dao.UsuarioDAO;
import com.ufes.delivery.model.Sessao;
import com.ufes.delivery.model.SituacaoUsuario;
import com.ufes.delivery.model.Usuario;
import com.ufes.delivery.util.SenhaUtil;
import java.sql.SQLException;
import java.util.Optional;

public class AutenticacaoService {

    private final UsuarioDAO usuarioDAO;
    private final IAuditoriaService auditoria;

    public AutenticacaoService(UsuarioDAO usuarioDAO, IAuditoriaService auditoria) {
        this.usuarioDAO = usuarioDAO;
        this.auditoria = auditoria;
    }

    /**
     * Autentica o usuário. Lança IllegalArgumentException com mensagem genérica
     * em caso de credencial inválida ou usuário bloqueado (US01 — não revela qual dado falhou).
     */
    public Usuario autenticar(String username, String senha) throws SQLException {
        // Validação de formato (client-side também faz, mas aqui é a regra de negócio)
        if (username == null || !username.matches("[a-z0-9]{3,30}")) {
            throw new IllegalArgumentException(
                "Nome de usuario deve usar letras minusculas e algarismos sem espacos");
        }

        Optional<Usuario> encontrado = usuarioDAO.buscarPorUsername(username);

        // Cenário 4 e 5 — mensagem genérica, não revela o motivo
        if (encontrado.isEmpty() || !SenhaUtil.verificar(senha, encontrado.get().getSenhaHash())) {
            auditoria.registrar(username, "LOGIN", "usuario:" + username,
                "Rejeitado", "Credenciais invalidas");
            throw new IllegalArgumentException("Credenciais invalidas");
        }

        Usuario usuario = encontrado.get();

        // US01 Cenário 5 — credenciais VÁLIDAS mas usuário Pendente/Não autorizado.
        // Aqui já não há vazamento sobre usuário/senha (ambos conferem), então a spec
        // pede a mensagem específica de autorização administrativa, distinta do
        // genérico "Credenciais invalidas" usado quando usuário/senha não conferem (Cenário 4).
        // AIDEV-NOTE: o sigilo da regra transversal vale para o caminho de credencial
        // inválida; o estado de autorização só é revelado após a credencial casar.
        if (!usuario.isAutorizado()) {
            auditoria.registrar(username, "LOGIN", "usuario:" + username,
                "Rejeitado", "Situacao: " + usuario.getSituacao().getDescricao());
            throw new IllegalArgumentException(
                "O acesso depende de autorizacao administrativa");
        }

        Sessao.getInstance().iniciar(usuario);
        auditoria.registrar(username, "LOGIN", "usuario:" + username,
            "Aprovado", "Perfil: " + usuario.getPerfil().getDescricao());

        return usuario;
    }
}
