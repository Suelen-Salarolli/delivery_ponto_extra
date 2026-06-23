package com.ufes.delivery.ui.produto;

import com.ufes.delivery.model.cadastro.Produto;
import com.ufes.delivery.service.ProdutoService;
import com.ufes.delivery.util.MoedaUtil;

import java.math.BigDecimal;

/**
 * Presenter de cadastro/edicao de produto (US07).
 */
public class TelaProdutoPresenter {

    private final TelaProduto view;
    private final ProdutoService produtoService;
    private Produto produtoEmEdicao; // null = novo

    public TelaProdutoPresenter(TelaProduto view, ProdutoService produtoService, Produto produto) {
        this.view = view;
        this.produtoService = produtoService;
        this.produtoEmEdicao = produto;
        vincularEventos();

        if (produto == null) {
            view.definirModoNovo();
        } else {
            view.preencher(produto);
        }
    }

    private void vincularEventos() {
        view.getBtnSalvar().addActionListener(e -> salvar());
        view.getBtnEditar().addActionListener(e -> view.definirModoLeitura(false));
        view.getBtnFechar().addActionListener(e -> view.dispose());
    }

    private void salvar() {
        view.setMensagemErro(" ");
        try {
            String codigoTxt = view.getCodigo();
            if (codigoTxt.isEmpty()) { view.setMensagemErro("Codigo e obrigatorio"); return; }
            if (view.getNome().isEmpty()) { view.setMensagemErro("Nome do produto e obrigatorio"); return; }
            if (view.getPreco().isEmpty()) { view.setMensagemErro("Preco unitario e obrigatorio"); return; }
            if (view.getEstoque().isEmpty()) { view.setMensagemErro("Estoque inicial e obrigatorio"); return; }

            int codigo = Integer.parseInt(codigoTxt);
            BigDecimal preco = MoedaUtil.parse(view.getPreco());
            int estoque = Integer.parseInt(view.getEstoque());

            if (produtoEmEdicao == null) {
                produtoEmEdicao = produtoService.cadastrar(
                    codigo, view.getNome(), view.getCategoria(), preco, estoque);
                view.preencher(produtoEmEdicao);
                view.setMensagemSucesso("Produto cadastrado com sucesso!");
            } else {
                produtoEmEdicao.setNome(view.getNome());
                produtoEmEdicao.setCategoria(view.getCategoria());
                produtoEmEdicao.setPrecoUnitario(preco);
                produtoEmEdicao.setEstoqueAtual(estoque);
                produtoService.atualizar(produtoEmEdicao);
                view.definirModoLeitura(true);
                view.setMensagemSucesso("Produto atualizado com sucesso!");
            }
        } catch (IllegalArgumentException ex) {
            view.setMensagemErro(ex.getMessage());
        } catch (Exception ex) {
            view.setMensagemErro("Erro ao salvar: " + ex.getMessage());
        }
    }
}
