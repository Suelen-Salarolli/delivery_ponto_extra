package com.ufes.delivery;

import com.ufes.delivery.auditoria.IAuditoriaService;
import com.ufes.delivery.auditoria.AuditoriaManager;
import com.ufes.delivery.dao.ClienteDAO;
import com.ufes.delivery.dao.PedidoResumoDAO;
import com.ufes.delivery.dao.ProdutoDAO;
import com.ufes.delivery.dao.UsuarioDAO;
import com.ufes.delivery.db.ConexaoDB;
import com.ufes.delivery.db.DemoSeed;
import com.ufes.delivery.model.Usuario;
import com.ufes.delivery.service.AutenticacaoService;
import com.ufes.delivery.service.ClienteService;
import com.ufes.delivery.service.PainelService;
import com.ufes.delivery.service.ProdutoService;
import com.ufes.delivery.service.UsuarioService;
import com.ufes.delivery.ui.login.TelaLogin;
import com.ufes.delivery.ui.login.TelaLoginPresenter;
import com.ufes.delivery.ui.painel.TelaPainel;
import com.ufes.delivery.ui.painel.TelaPainelPresenter;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // Look & feel nativo
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            try {
                // Inicializa banco e semeia pedidos demo (idempotente) para o painel.
                ConexaoDB.inicializarBanco();
                DemoSeed.semearPedidosSeVazio();

                // Dependências (composicao manual — sem container de DI).
                AuditoriaManager auditoriaManager = new AuditoriaManager();
                IAuditoriaService auditoria = auditoriaManager;
                UsuarioDAO usuarioDAO = new UsuarioDAO();
                AutenticacaoService autenticacaoService = new AutenticacaoService(usuarioDAO, auditoria);
                UsuarioService usuarioService = new UsuarioService(usuarioDAO, auditoria);
                ClienteService clienteService = new ClienteService(new ClienteDAO(), auditoria);
                ProdutoService produtoService = new ProdutoService(new ProdutoDAO(), auditoria);
                PainelService painelService = new PainelService(new PedidoResumoDAO());

                // Abre login
                iniciarFluxoLogin(autenticacaoService, usuarioService,
                    clienteService, produtoService, painelService, auditoria);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                    "Erro ao inicializar o sistema: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }

    private static void iniciarFluxoLogin(AutenticacaoService autenticacaoService,
                                          UsuarioService usuarioService,
                                          ClienteService clienteService,
                                          ProdutoService produtoService,
                                          PainelService painelService,
                                          com.ufes.delivery.auditoria.IAuditoriaService auditoria) {
        TelaLogin telaLogin = new TelaLogin(null);
        TelaLoginPresenter presenter = new TelaLoginPresenter(telaLogin, autenticacaoService, usuarioService);
        telaLogin.setVisible(true);

        Usuario autenticado = presenter.getUsuarioAutenticado();
        if (autenticado == null) {
            // Cancelou
            System.exit(0);
        }

        // US04 — apos login bem-sucedido abre o painel operacional para qualquer perfil.
        // O menu Administracao (US03) aparece somente para Administrador.
        abrirPainelOperacional(autenticado, painelService, clienteService,
            produtoService, usuarioService, auditoria);
    }

    private static void abrirPainelOperacional(Usuario usuario,
                                               PainelService painelService,
                                               ClienteService clienteService,
                                               ProdutoService produtoService,
                                               UsuarioService usuarioService,
                                               com.ufes.delivery.auditoria.IAuditoriaService auditoria) {
        TelaPainel telaPainel = new TelaPainel();
        new TelaPainelPresenter(telaPainel, painelService, clienteService,
            produtoService, usuarioService, usuario, auditoria);
        telaPainel.setVisible(true);
    }
}
