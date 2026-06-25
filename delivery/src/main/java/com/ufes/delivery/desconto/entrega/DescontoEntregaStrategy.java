package com.ufes.delivery.desconto.entrega;

import com.ufes.delivery.model.pedido.PedidoCadastro;
import java.math.BigDecimal;

/**
 * Padrao Strategy (comportamental) — cada forma de desconto sobre a taxa de
 * entrega encapsula um algoritmo proprio e intercambiavel.
 *
 * AIDEV-NOTE: opera sobre o agregado novo PedidoCadastro (cliente/endereco/itens
 * do pacote cadastro), substituindo o motor legado que usava model.Pedido.
 * Aplicar SOLID: novas formas de desconto entram como novas classes (OCP), sem
 * alterar a calculadora que as consome.
 */
public interface DescontoEntregaStrategy {

    /** Verdadeiro se esta forma de desconto se aplica ao pedido informado. */
    boolean seAplica(PedidoCadastro pedido);

    /** Valor do desconto (>= 0) que esta forma concede sobre a taxa de entrega. */
    BigDecimal calcular(PedidoCadastro pedido);

    /** Nome legivel da forma de desconto (para rastreabilidade/auditoria). */
    String nome();
}
