package com.ufes.delivery.service;

import com.ufes.delivery.auditoria.IAuditoriaService;
import com.ufes.delivery.dao.PagamentoDAO;
import com.ufes.delivery.model.Sessao;
import com.ufes.delivery.model.pagamento.ResultadoPagamento;
import com.ufes.delivery.model.pedido.PedidoCadastro;
import com.ufes.delivery.model.pedido.PedidoItem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Orquestra o fluxo de pagamento simulado (US10 + US11).
 *
 * Responsabilidades:
 *  1. Verificar disponibilidade de estoque no instante da confirmacao (US10)
 *  2. Delegar simulacao ao ISimuladorPagamento (Strategy — substituivel em testes)
 *  3. Se aprovado: baixa atomica de estoque + mudanca de estado (US10)
 *  4. Se reprovado: apenas registra tentativa, pedido fica preservado (US11)
 *  5. Registrar auditoria em ambos os casos (US12)
 */
public class PagamentoService {

    private static final DateTimeFormatter FMT_TXN =
        DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final PagamentoDAO pagamentoDAO;
    private final ISimuladorPagamento simulador;
    private final IAuditoriaService auditoria;

    public PagamentoService(PagamentoDAO pagamentoDAO,
                             ISimuladorPagamento simulador,
                             IAuditoriaService auditoria) {
        this.pagamentoDAO = pagamentoDAO;
        this.simulador = simulador;
        this.auditoria = auditoria;
    }

    /**
     * Processa uma tentativa de pagamento para o pedido informado.
     *
     * @return ResultadoPagamento com situacao Aprovado ou Reprovado.
     * @throws IllegalStateException se houver item sem estoque suficiente.
     */
    public ResultadoPagamento processar(PedidoCadastro pedido) {
        validarPedido(pedido);

        List<PedidoItem> itens = pedido.getItens();
        String usuario = Sessao.getInstance().getUsernameAtual();

        // US10 — verifica disponibilidade em tempo real antes de qualquer sorteio
        List<String> insuficientes;
        try {
            insuficientes = pagamentoDAO.verificarDisponibilidade(itens);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao verificar estoque: " + e.getMessage(), e);
        }

        if (!insuficientes.isEmpty()) {
            String detalhe = String.join("; ", insuficientes);
            auditoria.registrar(usuario, "PAGAMENTO_BLOQUEADO",
                "pedido:" + pedido.getCodigo(), "Bloqueado",
                "Estoque insuficiente: " + detalhe);
            throw new IllegalStateException(
                "Estoque insuficiente para um ou mais itens:\n" + detalhe);
        }

        LocalDateTime agora = LocalDateTime.now();
        boolean aprovado = simulador.sortearAprovacao();

        ResultadoPagamento resultado;

        if (aprovado) {
            String forma = simulador.sortearFormaPagamento();
            String txn = "TXN-" + agora.format(FMT_TXN) + "-" + pedido.getCodigo();
            LocalDateTime prazo = simulador.gerarPrazoEntrega(agora);

            // Padrao State: valida e aplica a transicao do ciclo do pedido.
            String estadoAnterior = pedido.getEstado().getDescricao();
            pedido.aprovarPagamento();

            resultado = new ResultadoPagamento(
                pedido.getId(), pedido.getCodigo(),
                ResultadoPagamento.Situacao.APROVADO,
                forma, txn, pedido.getValorTotal(), prazo, agora);

            try {
                // Transacao atomica: pagamento + baixa estoque + novo estado do pedido
                pagamentoDAO.confirmarAprovado(resultado, itens, pedido.getEstado());
            } catch (Exception e) {
                throw new RuntimeException("Erro ao confirmar pagamento: " + e.getMessage(), e);
            }

            auditoria.registrar(usuario, "PAGAMENTO_APROVADO",
                "pedido:" + pedido.getCodigo(), "Aprovado",
                "Forma: " + forma + " | Txn: " + txn);

            // Log transition of state
            auditoria.registrar(usuario, "TRANSICAO_ESTADO", "pedido:" + pedido.getCodigo(),
                "Sucesso", "De: " + estadoAnterior + " | Para: " + pedido.getEstado().getDescricao());

            // Auditoria da baixa de estoque por item
            for (PedidoItem item : itens) {
                auditoria.registrar(usuario, "BAIXA_ESTOQUE",
                    "produto:" + item.getProdutoCodigo() + " (" + item.getProdutoNome() + ")",
                    "Baixado", "Qtd:" + item.getQuantidade() + " | pedido:" + pedido.getCodigo());
            }

        } else {
            resultado = new ResultadoPagamento(
                pedido.getId(), pedido.getCodigo(),
                ResultadoPagamento.Situacao.REPROVADO,
                null, null, null, null, agora);

            try {
                pagamentoDAO.registrarReprovado(resultado);
            } catch (Exception e) {
                throw new RuntimeException("Erro ao registrar reprovacao: " + e.getMessage(), e);
            }

            auditoria.registrar(usuario, "PAGAMENTO_REPROVADO",
                "pedido:" + pedido.getCodigo(), "Reprovado",
                "Nenhuma alteracao de estoque ou estado realizada");
        }

        return resultado;
    }

    private void validarPedido(PedidoCadastro pedido) {
        if (pedido == null)
            throw new IllegalArgumentException("Pedido nao informado");
        if (pedido.getId() <= 0)
            throw new IllegalArgumentException("Pedido deve estar salvo antes de processar pagamento");
        if (pedido.getCliente() == null)
            throw new IllegalArgumentException("Cliente: cliente e obrigatorio");
        if (pedido.getEndereco() == null)
            throw new IllegalArgumentException("Endereco: endereco de entrega e obrigatorio");
        if (pedido.getItens().isEmpty())
            throw new IllegalArgumentException("Itens: pelo menos um item e obrigatorio");
    }
}
