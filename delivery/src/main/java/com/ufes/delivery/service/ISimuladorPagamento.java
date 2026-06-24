package com.ufes.delivery.service;

/**
 * Contrato do simulador de pagamento (US11).
 *
 * Separado do PagamentoService para que testes possam injetar
 * uma implementacao deterministica sem aleatoriedade.
 *
 * AIDEV-NOTE: padrao Strategy aplicado aqui — a fonte de aleatoriedade
 * e substituivel sem alterar o servico de negocio.
 */
public interface ISimuladorPagamento {

    /** Retorna true com probabilidade de 50%. */
    boolean sortearAprovacao();

    /**
     * Sorteia a forma de pagamento entre as 4 opcoes com peso igual (25% cada):
     * "Open Finance", "PIX chave", "PIX QR Code", "Cartao de credito".
     */
    String sortearFormaPagamento();

    /**
     * Gera prazo estimado de entrega: entre o instante de aprovacao
     * e o mesmo dia do mes subsequente.
     */
    java.time.LocalDateTime gerarPrazoEntrega(java.time.LocalDateTime instanteAprovacao);
}
