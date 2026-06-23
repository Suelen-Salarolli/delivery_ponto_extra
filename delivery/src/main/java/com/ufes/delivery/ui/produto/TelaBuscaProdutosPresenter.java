package com.ufes.delivery.ui.produto;

import com.ufes.delivery.model.cadastro.Produto;
import com.ufes.delivery.service.ProdutoService;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Presenter da busca de produtos (US07). Orquestra busca, visualizacao e novo cadastro.
 */
public class TelaBuscaProdutosPresenter {

    private final TelaBuscaProdutos view;
    private final ProdutoService produtoService;

    public TelaBuscaProdutosPresenter(TelaBuscaProdutos view, ProdutoService produtoService) {
        this.view = view;
        this.produtoService = produtoService;
        vincularEventos();
        carregarTodos();
    }

    private void vincularEventos() {
        view.getBtnBuscar().addActionListener(e -> buscar());
        view.getBtnNovo().addActionListener(e -> abrirNovo());
        view.getBtnVisualizar().addActionListener(e -> visualizar());
        view.getBtnFechar().addActionListener(e -> view.dispose());
        // Duplo clique na linha tambem abre o produto (US07 Cenario 5).
        view.getTabela().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) visualizar();
            }
        });
    }

    private void carregarTodos() {
        try {
            view.carregarProdutos(produtoService.listarTodos());
        } catch (Exception ex) {
            view.setMensagem("Erro ao carregar produtos: " + ex.getMessage(), true);
        }
    }

    private void buscar() {
        try {
            List<Produto> resultado = produtoService.buscar(view.getAtributoBusca(), view.getValorBusca());
            view.carregarProdutos(resultado);
            if (resultado.isEmpty()) {
                view.setMensagem("Nenhum produto encontrado para o criterio informado", false);
            } else {
                view.setMensagem(resultado.size() + " produto(s) encontrado(s)", false);
            }
        } catch (IllegalArgumentException ex) {
            view.setMensagem(ex.getMessage(), true);
        } catch (Exception ex) {
            view.setMensagem("Erro na busca: " + ex.getMessage(), true);
        }
    }

    private void visualizar() {
        Produto p = view.getProdutoSelecionado();
        if (p == null) {
            view.setMensagem("Selecione um produto para visualizar", true);
            return;
        }
        TelaProduto telaProduto = new TelaProduto((java.awt.Frame) SwingUtilities.getWindowAncestor(view));
        new TelaProdutoPresenter(telaProduto, produtoService, p);
        telaProduto.setVisible(true);
        carregarTodos();
    }

    private void abrirNovo() {
        TelaProduto telaProduto = new TelaProduto((java.awt.Frame) SwingUtilities.getWindowAncestor(view));
        new TelaProdutoPresenter(telaProduto, produtoService, null);
        telaProduto.setVisible(true);
        carregarTodos();
    }
}
