package com.ufes.delivery.util;

/**
 * Validacao e formatacao de CPF (US05/US06).
 *
 * AIDEV-NOTE: a validacao usa SOMENTE os digitos verificadores, conforme o texto
 * da spec ("validado pelos digitos verificadores"). Nao aplica a rejeicao extra de
 * sequencias repetidas (000.000.000-00, 111...) porque os cenarios de aceite da spec
 * (US05 C2, US06 C2) tratam "000.000.000-00" como CPF valido — e essa sequencia passa
 * no algoritmo dos digitos verificadores.
 */
public final class CpfUtil {

    private CpfUtil() {}

    /** Remove mascara, deixando somente digitos. */
    public static String normalizar(String cpf) {
        if (cpf == null) return "";
        return cpf.replaceAll("\\D", "");
    }

    /** Aplica mascara 000.000.000-00 sobre 11 digitos. */
    public static String formatar(String cpf) {
        String d = normalizar(cpf);
        if (d.length() != 11) return cpf;
        return d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9);
    }

    /** Verdadeiro se o CPF (com ou sem mascara) tem 11 digitos e verificadores validos. */
    public static boolean isValido(String cpf) {
        String d = normalizar(cpf);
        if (d.length() != 11) return false;

        int dig1 = calcularDigito(d, 9, 10);
        int dig2 = calcularDigito(d, 10, 11);
        return dig1 == (d.charAt(9) - '0') && dig2 == (d.charAt(10) - '0');
    }

    private static int calcularDigito(String digitos, int qtde, int pesoInicial) {
        int soma = 0;
        int peso = pesoInicial;
        for (int i = 0; i < qtde; i++) {
            soma += (digitos.charAt(i) - '0') * peso;
            peso--;
        }
        int resto = soma % 11;
        return (resto < 2) ? 0 : 11 - resto;
    }
}
