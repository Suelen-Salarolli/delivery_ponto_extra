package com.ufes.delivery.configuracao;

import java.time.LocalDate;

public final class ConfiguracaoService {
    private static final double TAXA_ENTREGA_PADRAO = 10.00;

    
    private static final LocalDate DATA_OPERACAO = LocalDate.now();

    private ConfiguracaoService() {
    }

    public static double getTaxaEntregaPadrao() {
        return TAXA_ENTREGA_PADRAO;
    }

    public static LocalDate getDataOperacao() {
        return DATA_OPERACAO;
    }
}
