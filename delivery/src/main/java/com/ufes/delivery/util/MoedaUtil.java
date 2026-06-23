package com.ufes.delivery.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Formatacao e parsing de valores monetarios no padrao brasileiro
 * (virgula decimal, duas casas). Regra transversal "Dados monetarios".
 */
public final class MoedaUtil {

    private static final Locale BR = Locale.forLanguageTag("pt-BR");
    private static final DecimalFormatSymbols SIMBOLOS = new DecimalFormatSymbols(BR);

    private MoedaUtil() {}

    /** Formata como "R$ 1.234,56". */
    public static String formatar(BigDecimal valor) {
        if (valor == null) return "";
        DecimalFormat df = new DecimalFormat("#,##0.00", SIMBOLOS);
        return "R$ " + df.format(valor);
    }

    /** Formata sem prefixo: "1.234,56". */
    public static String formatarSemSimbolo(BigDecimal valor) {
        if (valor == null) return "";
        DecimalFormat df = new DecimalFormat("#,##0.00", SIMBOLOS);
        return df.format(valor);
    }

    /**
     * Converte texto no formato brasileiro ("18,50", "1.234,56", "R$ 18,50")
     * para BigDecimal com duas casas. Lanca IllegalArgumentException se invalido.
     */
    public static BigDecimal parse(String texto) {
        if (texto == null || texto.isBlank())
            throw new IllegalArgumentException("Valor monetario e obrigatorio");
        String limpo = texto.trim()
                .replace("R$", "")
                .replace(" ", "")
                .replace(".", "")   // separador de milhar
                .replace(",", ".")  // separador decimal
                .trim();
        try {
            return new BigDecimal(limpo).setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Valor monetario deve ter no maximo duas casas decimais");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor monetario invalido");
        }
    }
}
