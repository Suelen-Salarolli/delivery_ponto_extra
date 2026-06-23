package com.ufes.delivery.ui.login;

import com.ufes.delivery.model.Usuario;
import com.ufes.delivery.service.AutenticacaoService;
import com.ufes.delivery.ui.usuario.TelaCadastroUsuario;
import com.ufes.delivery.ui.usuario.TelaCadastroUsuarioPresenter;
import com.ufes.delivery.service.UsuarioService;

import javax.swing.*;
import java.awt.*;

public class TelaLoginPresenter {

    private final TelaLogin view;
    private final AutenticacaoService autenticacaoService;
    private final UsuarioService usuarioService;
    private Usuario usuarioAutenticado;

    public TelaLoginPresenter(TelaLogin view, AutenticacaoService autenticacaoService,
                              UsuarioService usuarioService) {
        this.view = view;
        this.autenticacaoService = autenticacaoService;
        this.usuarioService = usuarioService;
        vincularEventos();
    }

    private void vincularEventos() {
        view.getBtnAcessar().addActionListener(e -> acessar());
        view.getBtnCancelar().addActionListener(e -> view.dispose());
        view.getBtnCadastrar().addActionListener(e -> abrirCadastro());
    }

    private void acessar() {
        view.setMensagemErro(" ");
        String username = view.getUsername();
        String senha = view.getSenha();

        // Validação de formato no presenter (antes de chamar o serviço)
        if (username.isEmpty() || senha.isEmpty()) {
            view.setMensagemErro("Informe usuario e senha");
            return;
        }

        if (!username.matches("[a-z0-9]+")) {
            view.setMensagemErro("Nome de usuario deve usar letras minusculas e algarismos sem espacos");
            view.limparSenha();
            return;
        }

        try {
            usuarioAutenticado = autenticacaoService.autenticar(username, senha);
            view.dispose();
        } catch (IllegalArgumentException ex) {
            view.setMensagemErro(ex.getMessage());
            view.limparSenha();
        } catch (Exception ex) {
            view.setMensagemErro("Erro interno: " + ex.getMessage());
            view.limparSenha();
        }
    }

    private void abrirCadastro() {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(view);
        TelaCadastroUsuario telaCadastro = new TelaCadastroUsuario(owner);
        new TelaCadastroUsuarioPresenter(telaCadastro, usuarioService);
        telaCadastro.setVisible(true);
    }

    public Usuario getUsuarioAutenticado() {
        return usuarioAutenticado;
    }
}
