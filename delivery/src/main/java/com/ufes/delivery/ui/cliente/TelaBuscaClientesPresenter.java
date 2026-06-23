package com.ufes.delivery.ui.cliente;

import com.ufes.delivery.model.cadastro.Cliente;
import com.ufes.delivery.service.ClienteService;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Presenter da busca de clientes (US05). Orquestra busca, visualizacao e novo cadastro.
 */
public class TelaBuscaClientesPresenter {

    private final TelaBuscaClientes view;
    private final ClienteService clienteService;

    public TelaBuscaClientesPresenter(TelaBuscaClientes view, ClienteService clienteService) {
        this.view = view;
        this.clienteService = clienteService;
        vincularEventos();
        carregarTodos();
    }

    private void vincularEventos() {
        view.getBtnBuscar().addActionListener(e -> buscar());
        view.getBtnNovo().addActionListener(e -> abrirNovo());
        view.getBtnVisualizar().addActionListener(e -> visualizar());
        view.getBtnFechar().addActionListener(e -> view.dispose());
        view.getTabela().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) visualizar();
            }
        });
    }

    private void carregarTodos() {
        try {
            view.carregarClientes(clienteService.listarTodos());
        } catch (Exception ex) {
            view.setMensagem("Erro ao carregar clientes: " + ex.getMessage(), true);
        }
    }

    private void buscar() {
        try {
            List<Cliente> resultado = clienteService.buscar(view.getAtributoBusca(), view.getValorBusca());
            view.carregarClientes(resultado);
            if (resultado.isEmpty()) {
                view.setMensagem("Nao ha clientes para o criterio informado", false);
            } else {
                view.setMensagem(resultado.size() + " cliente(s) encontrado(s)", false);
            }
        } catch (IllegalArgumentException ex) {
            view.setMensagem(ex.getMessage(), true);
        } catch (Exception ex) {
            view.setMensagem("Erro na busca: " + ex.getMessage(), true);
        }
    }

    private void visualizar() {
        Cliente selecionado = view.getClienteSelecionado();
        if (selecionado == null) {
            view.setMensagem("Selecione um cliente para visualizar", true);
            return;
        }
        try {
            // Recarrega com enderecos (a lista da busca traz somente nome e CPF).
            Cliente completo = clienteService.buscarPorId(selecionado.getId()).orElse(selecionado);
            TelaCliente telaCliente = new TelaCliente((java.awt.Frame) SwingUtilities.getWindowAncestor(view));
            new TelaClientePresenter(telaCliente, clienteService, completo);
            telaCliente.setVisible(true);
            carregarTodos();
        } catch (Exception ex) {
            view.setMensagem("Erro ao abrir cliente: " + ex.getMessage(), true);
        }
    }

    private void abrirNovo() {
        TelaCliente telaCliente = new TelaCliente((java.awt.Frame) SwingUtilities.getWindowAncestor(view));
        new TelaClientePresenter(telaCliente, clienteService, null);
        telaCliente.setVisible(true);
        carregarTodos();
    }
}
