package com.ufes.delivery.ui.usuario;

import com.ufes.delivery.model.Usuario;
import com.ufes.delivery.service.UsuarioService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TelaUsuariosPresenter {

    private final TelaUsuarios view;
    private final UsuarioService usuarioService;

    public TelaUsuariosPresenter(TelaUsuarios view, UsuarioService usuarioService) {
        this.view = view;
        this.usuarioService = usuarioService;
        vincularEventos();
        carregarTodos();
    }

    private void vincularEventos() {
        view.getBtnBuscar().addActionListener(e -> buscar());
        view.getCampoBusca().addActionListener(e -> buscar());
        view.getBtnAutorizar().addActionListener(e -> autorizar());
        view.getBtnDesautorizar().addActionListener(e -> desautorizar());
        view.getBtnExcluir().addActionListener(e -> excluir());
        view.getBtnNovo().addActionListener(e -> abrirCadastro());
        view.getBtnFechar().addActionListener(e -> view.dispose());
    }

    private void carregarTodos() {
        try {
            view.carregarUsuarios(usuarioService.listarTodos());
        } catch (Exception ex) {
            view.setMensagem("Erro ao carregar usuarios: " + ex.getMessage(), true);
        }
    }

    private void buscar() {
        try {
            String filtro = view.getFiltroBusca();
            List<Usuario> resultado = usuarioService.buscarPorNome(filtro);
            view.carregarUsuarios(resultado);
            if (resultado.isEmpty()) {
                view.setMensagem("Nenhum usuario encontrado para o criterio informado", false);
            } else {
                view.setMensagem(resultado.size() + " usuario(s) encontrado(s)", false);
            }
        } catch (Exception ex) {
            view.setMensagem("Erro na busca: " + ex.getMessage(), true);
        }
    }

    private void autorizar() {
        List<Usuario> selecionados = view.getUsuariosSelecionados();
        if (selecionados.isEmpty()) {
            view.setMensagem("Selecione pelo menos um usuario", true);
            return;
        }
        try {
            usuarioService.autorizar(selecionados);
            carregarTodos();
            view.setMensagem(selecionados.size() + " usuario(s) autorizado(s) com sucesso", false);
        } catch (Exception ex) {
            view.setMensagem("Erro ao autorizar: " + ex.getMessage(), true);
        }
    }

    private void desautorizar() {
        List<Usuario> selecionados = view.getUsuariosSelecionados();
        if (selecionados.isEmpty()) {
            view.setMensagem("Selecione pelo menos um usuario", true);
            return;
        }
        try {
            usuarioService.desautorizar(selecionados);
            carregarTodos();
            view.setMensagem(selecionados.size() + " usuario(s) desautorizado(s)", false);
        } catch (Exception ex) {
            view.setMensagem("Erro ao desautorizar: " + ex.getMessage(), true);
        }
    }

    private void excluir() {
        List<Usuario> selecionados = view.getUsuariosSelecionados();
        if (selecionados.isEmpty()) {
            view.setMensagem("Selecione pelo menos um usuario", true);
            return;
        }
        int conf = JOptionPane.showConfirmDialog(view,
            "Deseja excluir " + selecionados.size() + " usuario(s) selecionado(s)?",
            "Confirmar exclusao", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;

        try {
            usuarioService.excluir(selecionados);
            carregarTodos();
            view.setMensagem(selecionados.size() + " usuario(s) excluido(s)", false);
        } catch (Exception ex) {
            view.setMensagem("Erro ao excluir: " + ex.getMessage(), true);
        }
    }

    private void abrirCadastro() {
        TelaCadastroUsuario telaCadastro = new TelaCadastroUsuario(
            (Frame) SwingUtilities.getWindowAncestor(view));
        new TelaCadastroUsuarioPresenter(telaCadastro, usuarioService);
        telaCadastro.setVisible(true);
        carregarTodos(); // Atualiza após possível cadastro
    }
}
