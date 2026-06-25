package com.ufes.delivery.ui.pedido;

import com.ufes.delivery.configuracao.ConfiguracaoService;
import com.ufes.delivery.model.cadastro.Cliente;
import com.ufes.delivery.model.cadastro.Endereco;
import com.ufes.delivery.model.cadastro.Produto;
import com.ufes.delivery.model.pedido.PedidoCadastro;
import com.ufes.delivery.model.pedido.PedidoItem;
import com.ufes.delivery.service.ClienteService;
import com.ufes.delivery.service.PagamentoService;
import com.ufes.delivery.service.PedidoService;
import com.ufes.delivery.service.ProdutoService;
import com.ufes.delivery.ui.pagamento.TelaPagamentoPresenter;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Presenter da tela de pedido (US09 + US10/US11).
 *
 * Fluxo:
 *  1. Carrega clientes e produtos ao abrir
 *  2. Ao selecionar cliente, carrega enderecos e pre-seleciona o padrao
 *  3. Adicionar/remover itens e recalcular totais em tempo real
 *  4. Ao clicar Pagar:
 *     a. Salva o pedido (persiste com estado Novo)
 *     b. Abre TelaPagamento que processa a simulacao
 *     c. Se aprovado -> fecha a tela do pedido e atualiza painel
 *     d. Se reprovado -> permanece na tela para nova tentativa (pedido ja salvo)
 */
public class TelaPedidoPresenter {

    private final TelaPedido view;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;
    private final PedidoService pedidoService;
    private final PagamentoService pagamentoService;
    private final Runnable aoFinalizar;

    // Pedido persistido apos primeiro clique em Pagar (evita salvar duplicado)
    private PedidoCadastro pedidoSalvo;
    private boolean atualizandoTabela;

    public TelaPedidoPresenter(TelaPedido view,
                                ClienteService clienteService,
                                ProdutoService produtoService,
                                PedidoService pedidoService,
                                PagamentoService pagamentoService,
                                Runnable aoFinalizar) {
        this.view = view;
        this.clienteService = clienteService;
        this.produtoService = produtoService;
        this.pedidoService = pedidoService;
        this.pagamentoService = pagamentoService;
        this.aoFinalizar = aoFinalizar;
        vincularEventos();
        carregarDados();
        recalcularTotais();
    }

    private void vincularEventos() {
        view.getComboClientes().addActionListener(e -> carregarEnderecosCliente());
        view.getBtnAdicionarItem().addActionListener(e -> adicionarItem());
        view.getMenuExcluirItem().addActionListener(e -> removerItem());
        view.getCampoCupom().addActionListener(e -> recalcularTotais());
        view.getBtnPagar().addActionListener(e -> pagar());
        view.getBtnCancelar().addActionListener(e -> cancelar());
        view.getModeloItens().addTableModelListener(e -> {
            if (!atualizandoTabela && e.getType() == TableModelEvent.UPDATE && e.getColumn() == 3) {
                atualizarQuantidade(e.getFirstRow());
            }
        });
    }

    private void carregarDados() {
        try {
            view.carregarClientes(clienteService.listarTodos());
            List<Produto> produtos = produtoService.listarTodos();
            view.carregarProdutos(produtos);
            if (produtos.isEmpty())
                view.setMensagem("Produto: cadastre produtos antes de criar pedido");
            carregarEnderecosCliente();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view,
                "Erro ao carregar dados: " + ex.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarEnderecosCliente() {
        Cliente selecionado = view.getClienteSelecionado();
        if (selecionado == null) {
            view.carregarEnderecos(List.of());
            return;
        }
        try {
            Cliente completo = clienteService.buscarPorId(selecionado.getId())
                .orElse(selecionado);
            view.carregarEnderecos(completo.getEnderecos());
            Endereco padrao = completo.getEnderecoPadrao();
            if (padrao != null) view.selecionarEndereco(padrao);
        } catch (Exception ex) {
            view.setMensagem("Endereco: erro ao carregar enderecos do cliente");
        }
        recalcularTotais();
    }

    private void adicionarItem() {
        try {
            Produto produto = view.getProdutoSelecionado();
            if (produto == null)
                throw new IllegalArgumentException("Produto: selecione um produto");
            view.adicionarItem(new PedidoItem(produto, view.getQuantidade()));
            pedidoSalvo = null; // pedido foi modificado — precisara salvar novamente
            recalcularTotais();
            view.setMensagem(" ");
        } catch (IllegalArgumentException ex) {
            view.setMensagem(ex.getMessage());
        }
    }

    private void removerItem() {
        view.removerItemSelecionado();
        pedidoSalvo = null;
        recalcularTotais();
    }

    private void atualizarQuantidade(int linha) {
        try {
            Object valor = view.getModeloItens().getValueAt(linha, 3);
            int qtd = valor instanceof Number n ? n.intValue()
                : Integer.parseInt(String.valueOf(valor));
            view.atualizarQuantidade(linha, qtd);
            pedidoSalvo = null;
            recalcularTotais();
        } catch (Exception ex) {
            view.setMensagem("Quantidade: informe um numero inteiro maior que zero");
            atualizandoTabela = true;
            try { view.getModeloItens().fireTableDataChanged(); }
            finally { atualizandoTabela = false; }
        }
    }

    private void recalcularTotais() {
        try {
            PedidoCadastro pedido = pedidoService.montarPedido(
                view.getClienteSelecionado(),
                view.getEnderecoSelecionado(),
                view.getItens(),
                view.getCupom(),
                ConfiguracaoService.getDataOperacao()
            );
            view.setTotais(pedido.getSubtotalItens(), pedido.getDescontoItens(),
                pedido.getTaxaEntregaFinal(), pedido.getValorTotal());
        } catch (Exception ex) {
            // Silencioso durante montagem — erros aparecem so ao clicar Pagar
            BigDecimal sub = view.getItens().stream()
                .map(PedidoItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            view.setTotais(sub, BigDecimal.ZERO, BigDecimal.ZERO, sub);
        }
    }

    private void pagar() {
        view.setMensagem(" ");

        // Valida e persiste o pedido se ainda nao foi salvo (ou se foi modificado)
        if (pedidoSalvo == null) {
            try {
                pedidoSalvo = pedidoService.salvarNovoPedido(
                    view.getClienteSelecionado(),
                    view.getEnderecoSelecionado(),
                    view.getItens(),
                    view.getCupom(),
                    ConfiguracaoService.getDataOperacao()
                );
            } catch (IllegalArgumentException ex) {
                view.setMensagem(ex.getMessage());
                return;
            } catch (Exception ex) {
                view.setMensagem("Erro ao registrar pedido: " + ex.getMessage());
                return;
            }
        }

        // Processa pagamento simulado (US10/US11)
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(view);
        boolean aprovado = TelaPagamentoPresenter.processar(
            owner, pedidoSalvo, pagamentoService,
            () -> { /* callback executado pelo presenter se aprovado */ }
        );

        if (aprovado) {
            // Pedido aprovado e estoque baixado — fecha a tela e atualiza painel
            view.dispose();
            if (aoFinalizar != null) aoFinalizar.run();
        }
        // Se reprovado: permanece na tela com pedido preservado para nova tentativa
    }

    private void cancelar() {
        if (!view.getItens().isEmpty()) {
            int conf = JOptionPane.showConfirmDialog(view,
                "Deseja cancelar o pedido em elaboracao?",
                "Cancelar pedido", JOptionPane.YES_NO_OPTION);
            if (conf != JOptionPane.YES_OPTION) return;
        }
        view.dispose();
    }
}
