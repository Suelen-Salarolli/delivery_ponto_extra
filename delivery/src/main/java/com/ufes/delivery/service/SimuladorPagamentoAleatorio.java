package com.ufes.delivery.service;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Simulador de producao: aleatoriedade real (US11).
 * 50% aprovado/reprovado, 4 formas de pagamento com 25% cada.
 */
public class SimuladorPagamentoAleatorio implements ISimuladorPagamento {

    private static final String[] FORMAS = {
        "Open Finance", "PIX chave", "PIX QR Code", "Cartao de crédito"
    };

    private final Random random;

    public SimuladorPagamentoAleatorio() {
        this.random = new Random();
    }

    @Override
    public boolean sortearAprovacao() {
        return random.nextBoolean(); // 50/50
    }

    @Override
    public String sortearFormaPagamento() {
        return FORMAS[random.nextInt(FORMAS.length)]; // 25% cada
    }

    @Override
    public LocalDateTime gerarPrazoEntrega(LocalDateTime instanteAprovacao) {
        // Entre o instante de aprovacao e o mesmo dia do mes subsequente
        LocalDateTime limite = instanteAprovacao.plusMonths(1);
        long segundosDisp = java.time.Duration.between(instanteAprovacao, limite).getSeconds();
        long segundosAleatorio = segundosDisp > 0
            ? (long) (random.nextDouble() * segundosDisp)
            : 0;
        return instanteAprovacao.plusSeconds(segundosAleatorio);
    }
}
