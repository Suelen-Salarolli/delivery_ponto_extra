package com.ufes.delivery.ui.estoque;

import com.ufes.delivery.model.cadastro.MovimentacaoEstoque;
import com.ufes.delivery.model.cadastro.Produto;
import com.ufes.delivery.service.EstoqueService;
import com.ufes.delivery.service.ProdutoService;

import java.time.LocalDate;
import java.util.List;

/**
 * Presenter de movimentacao de estoque (US08). Segue MVP Passive View:
 * toda logica de validacao e orquestracao fica aqui; a view so exibe e notifica.
 */
public class TelaMovimentacaoEstoquePresenter {

    private final TelaMovimentacaoEstoque view;
    private final ProdutoService produtoService;
    private final EstoqueService estoqueService;

    private Produto produtoSelecionado;

    public TelaMovimentacaoEstoquePresenter(TelaMovimentacaoEstoque view,
                                            ProdutoService produtoService,
                                            EstoqueService estoqueService) {
        this.view = view;
        this.produtoService = produtoService;
        this.estoqueService = estoqueService;
        vincularEventos();
        // Inicia com tipo Entrada — ativa/desativa campos correspondentes
        view.atualizarCamposPorTipo(MovimentacaoEstoque.TIPO_ENTRADA);
    }

    private void vincularEventos() {
        view.getBtnBuscar().addActionListener(e -> buscarProduto());
        view.getCampoBuscaProduto().addActionListener(e -> buscarProduto());
        view.getBtnSelecionar().addActionListener(e -> selecionarProduto());
        view.getBtnConfirmar().addActionListener(e -> confirmar());
        view.getBtnCancelar().addActionListener(e -> view.dispose());

        // Atualiza previa ao mudar quantidade
        view.getSpinnerQuantidade().addChangeListener(e -> atualizarPrevia());

        // Alterna campos obrigatorios ao mudar tipo
        view.getComboTipo().addActionListener(e -> {
            view.atualizarCamposPorTipo(view.getTipo());
            atualizarPrevia();
        });
    }

    private void buscarProduto() {
        view.setMensagemErro(" ");
        String texto = view.getTextoBusca();
        if (texto.isEmpty()) {
            view.setMensagemErro("Informe o nome do produto para buscar");
            return;
        }
        try {
            List<Produto> resultado = produtoService.buscar(
                ProdutoService.AtributoBusca.NOME, texto);
            view.carregarProdutos(resultado);
            if (resultado.isEmpty())
                view.setMensagemErro("Nenhum produto encontrado para \"" + texto + "\"");
        } catch (Exception ex) {
            view.setMensagemErro("Erro na busca: " + ex.getMessage());
        }
    }

    private void selecionarProduto() {
        Produto p = view.getProdutoDaBuscaSelecionado();
        if (p == null) {
            view.setMensagemErro("Selecione um produto na tabela");
            return;
        }
        produtoSelecionado = p;
        view.exibirProdutoSelecionado(p);
        view.setMensagemErro(" ");
        atualizarPrevia();
    }

    private void atualizarPrevia() {
        if (produtoSelecionado == null) return;
        try {
            int previa = estoqueService.calcularPrevia(
                produtoSelecionado, view.getQuantidade(), view.getDataMovimentacao());
            view.setPrevia(previa);
            view.setMensagemErro(" ");
        } catch (IllegalArgumentException ex) {
            view.setPrevia(-1); // sinaliza invalido visualmente
            view.setMensagemErro(ex.getMessage());
        }
    }

    private void confirmar() {
        view.setMensagemErro(" ");

        if (produtoSelecionado == null) {
            view.setMensagemErro("Selecione um produto antes de confirmar");
            return;
        }

        LocalDate data = view.getDataMovimentacao();
        if (data == null) {
            view.setMensagemErro("Data da movimentacao invalida. Use o formato DD/MM/AAAA");
            return;
        }

        try {
            estoqueService.confirmar(
                produtoSelecionado,
                view.getQuantidade(),
                view.getTipo(),
                data,
                view.getMotivo(),
                view.getNotaFiscal()
            );
            // Atualiza o card do produto selecionado com o novo estoque
            view.exibirProdutoSelecionado(produtoSelecionado);
            view.setMensagemSucesso("Movimentacao confirmada! Novo estoque: "
                + produtoSelecionado.getEstoqueAtual());
            view.getBtnConfirmar().setEnabled(false);
        } catch (IllegalArgumentException ex) {
            view.setMensagemErro(ex.getMessage());
        } catch (Exception ex) {
            view.setMensagemErro("Erro ao confirmar movimentacao: " + ex.getMessage());
        }
    }
}
