package com.ufes.delivery.model.pedido.estado;

import com.ufes.delivery.model.cadastro.EstadoPedido;

/**
 * Padrao State (comportamental) — encapsula as transicoes legais do ciclo de vida
 * do pedido. Cada estado concreto sobrescreve apenas as acoes permitidas; uma acao
 * ilegal lanca IllegalStateException (mesma ideia do exemplo da porta no material
 * de Padroes de Projeto do professor).
 *
 * AIDEV-NOTE: o enum EstadoPedido continua sendo o valor PERSISTIDO e exibido.
 * Esta camada guarda as REGRAS de transicao. Use de(enum) para obter o estado e
 * valor() para voltar ao enum. A unica transicao acionada pelo sistema hoje e a
 * aprovacao de pagamento (US10); as demais etapas logisticas do ciclo do pedido
 * (despacho/entrega) ainda nao tem acao de usuario que as dispare.
 */
public abstract class EstadoPedidoState {

    /** Valor de dominio (enum) correspondente a este estado. */
    public abstract EstadoPedido valor();

    /** Transicao apos pagamento aprovado (US10). So e legal a partir de Novo. */
    public EstadoPedidoState aprovarPagamento() {
        throw new IllegalStateException(
            "Nao e possivel aprovar pagamento de um pedido " + valor().getDescricao());
    }

    /** Fabrica o estado concreto correspondente ao enum. */
    public static EstadoPedidoState de(EstadoPedido estado) {
        return switch (estado) {
            case NOVO -> new NovoState();
            case AGUARDANDO_PAGAMENTO -> new AguardandoPagamentoState();
            case EM_PREPARO -> new EmPreparoState();
            case AGUARDANDO_ENTREGA -> new AguardandoEntregaState();
            case EM_TRANSITO -> new EmTransitoState();
            case ENTREGUE -> new EntregueState();
        };
    }
}
