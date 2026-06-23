package com.ufes.delivery.configuracao;

import java.time.LocalDate;

public final class ConfiguracaoService {
    private static final double TAXA_ENTREGA_PADRAO = 10.00;

    // AIDEV-NOTE: data de operacao fixa do POC (US04). Alinha o painel com os
    // cenarios de aceite (20/06/2026) e com o seed demo. Na Fase 3/4 passa a
    // derivar do relogio/data de negocio quando houver pedidos reais.
    private static final LocalDate DATA_OPERACAO = LocalDate.of(2026, 6, 20);

    private ConfiguracaoService() {
    }

    public static double getTaxaEntregaPadrao() {
        return TAXA_ENTREGA_PADRAO;
    }

    public static LocalDate getDataOperacao() {
        return DATA_OPERACAO;
    }
}
