package com.ufes.delivery.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SenhaUtil {

    private SenhaUtil() {}

    public static String hash(String senha) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(senha.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao calcular hash da senha", e);
        }
    }

    public static boolean verificar(String senhaInformada, String hashArmazenado) {
        return hash(senhaInformada).equals(hashArmazenado);
    }

    public static void validarTamanho(String senha) {
        if (senha == null || senha.length() < 8 || senha.length() > 64) {
            throw new IllegalArgumentException("Senha deve ter entre 8 e 64 caracteres");
        }
    }
}
