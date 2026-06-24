package com.ufes.delivery.ui.pagamento;

import com.ufes.delivery.model.pagamento.ResultadoPagamento;
import com.ufes.delivery.model.pedido.PedidoCadastro;
import com.ufes.delivery.service.PagamentoService;

import javax.swing.*;
import java.awt.*;

/**
 * Presenter da tela de pagamento (US10/US11).
 * Chama o PagamentoService e exibe o resultado — sem logica de negocio aqui.
 */
public class TelaPagamentoPresenter {

    private final TelaPagamento view;
    private final boolean aprovado;

    public TelaPagamentoPresenter(TelaPagamento view, boolean aprovado) {
        this.view = view;
        this.aprovado = aprovado;
        vincularEventos();
    }

    private void vincularEventos() {
        view.getBtnFechar().addActionListener(e -> view.dispose());
    }

    public boolean isAprovado() { return aprovado; }

    /**
     * Abre a tela de pagamento processando o pedido.
     * Retorna true se aprovado, false se reprovado ou erro de estoque.
     *
     * @param owner     janela pai
     * @param pedido    pedido ja persistido com id valido
     * @param service   servico de pagamento
     * @param aoAprovado callback executado se pagamento for aprovado (ex: atualizar painel)
     */
    public static boolean processar(Frame owner, PedidoCadastro pedido,
                                     PagamentoService service, Runnable aoAprovado) {
        ResultadoPagamento resultado;
        try {
            resultado = service.processar(pedido);
        } catch (IllegalStateException ex) {
            // Estoque insuficiente — nao abre a tela de pagamento
            JOptionPane.showMessageDialog(owner,
                ex.getMessage(), "Estoque insuficiente", JOptionPane.WARNING_MESSAGE);
            return false;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner,
                "Erro ao processar pagamento: " + ex.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        TelaPagamento tela = new TelaPagamento(owner, pedido, resultado);
        TelaPagamentoPresenter presenter = new TelaPagamentoPresenter(tela, resultado.isAprovado());
        tela.setVisible(true); // bloqueia ate fechar

        if (resultado.isAprovado() && aoAprovado != null) {
            aoAprovado.run();
        }

        return resultado.isAprovado();
    }
}
