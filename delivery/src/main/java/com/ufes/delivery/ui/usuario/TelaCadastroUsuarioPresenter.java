package com.ufes.delivery.ui.usuario;

import com.ufes.delivery.service.UsuarioService;
import javax.swing.JOptionPane;

public class TelaCadastroUsuarioPresenter {

    private final TelaCadastroUsuario view;
    private final UsuarioService usuarioService;

    public TelaCadastroUsuarioPresenter(TelaCadastroUsuario view, UsuarioService usuarioService) {
        this.view = view;
        this.usuarioService = usuarioService;
        vincularEventos();
    }

    private void vincularEventos() {
        view.getBtnConfirmar().addActionListener(e -> confirmar());
        view.getBtnCancelar().addActionListener(e -> view.dispose());
    }

    private void confirmar() {
        view.setMensagemErro(" ");
        String nome = view.getNome();
        String username = view.getUsername();
        String senha = view.getSenha();

        // Validações de campo obrigatório
        if (nome.isEmpty()) {
            view.setMensagemErro("Nome e obrigatorio");
            return;
        }
        if (username.isEmpty()) {
            view.setMensagemErro("Nome de usuario e obrigatorio");
            return;
        }
        if (senha.isEmpty()) {
            view.setMensagemErro("Senha e obrigatoria");
            return;
        }

        try {
            usuarioService.cadastrar(nome, username, senha);
            JOptionPane.showMessageDialog(
                view,
                "Usuario cadastrado com sucesso!",
                "Cadastro realizado",
                JOptionPane.INFORMATION_MESSAGE
            );
            view.dispose();
        } catch (IllegalArgumentException ex) {
            view.setMensagemErro(ex.getMessage());
        } catch (Exception ex) {
            view.setMensagemErro("Erro ao cadastrar: " + ex.getMessage());
        }
    }
}
