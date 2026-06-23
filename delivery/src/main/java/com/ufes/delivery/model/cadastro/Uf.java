package com.ufes.delivery.model.cadastro;

/**
 * Unidades federativas brasileiras (US06 — UF deve ser sigla valida de duas letras).
 */
public enum Uf {
    AC, AL, AP, AM, BA, CE, DF, ES, GO, MA, MT, MS, MG, PA, PB, PR, PE, PI,
    RJ, RN, RS, RO, RR, SC, SP, SE, TO;

    public static boolean isValida(String sigla) {
        if (sigla == null) return false;
        for (Uf uf : values()) {
            if (uf.name().equalsIgnoreCase(sigla.trim())) return true;
        }
        return false;
    }

    public static Uf de(String sigla) {
        if (!isValida(sigla))
            throw new IllegalArgumentException("UF invalida: " + sigla);
        return valueOf(sigla.trim().toUpperCase());
    }
}
