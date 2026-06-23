package com.ufes.delivery.auditoria;

import com.ufes.delivery.db.ConexaoDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditoriaDBService implements IAuditoriaService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    public void registrar(String usuario, String operacao, String recurso,
                          String resultado, String justificativa) {
        String sql = """
            INSERT INTO auditoria (usuario, data_hora, operacao, recurso, resultado, justificativa)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, LocalDateTime.now().format(FMT));
            ps.setString(3, operacao);
            ps.setString(4, recurso);
            ps.setString(5, resultado);
            ps.setString(6, justificativa);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("AVISO: falha ao registrar auditoria - " + e.getMessage());
        }
    }
}
