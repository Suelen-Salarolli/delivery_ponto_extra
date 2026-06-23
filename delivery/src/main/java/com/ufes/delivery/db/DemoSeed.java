package com.ufes.delivery.db;

import com.ufes.delivery.configuracao.ConfiguracaoService;
import com.ufes.delivery.model.cadastro.EstadoPedido;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;

/**
 * Seed idempotente de dados demo para o painel (US04).
 * Cria um cliente demo e 8 pedidos na data de operacao para tornar o painel testavel.
 *
 * AIDEV-NOTE: na Fase 3/4 os pedidos reais passam a usar o fluxo US09/US10.
 * Este seed continua util para o cenario de aceite do US04 (painel com metricas).
 */
public final class DemoSeed {

    private DemoSeed() {}

    public static void semearPedidosSeVazio() {
        try (Connection conn = ConexaoDB.getConexao()) {
            // Verifica se ja existem pedidos
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM pedidos")) {
                if (rs.next() && rs.getInt(1) > 0) return;
            }

            conn.setAutoCommit(false);
            try {
                // Cria cliente demo (se nao existir)
                int clienteId = obterOuCriarClienteDemo(conn);
                int enderecoId = obterOuCriarEnderecoDemo(conn, clienteId);

                LocalDate hoje = ConfiguracaoService.getDataOperacao();
                inserir(conn, 1001, "Fulano de Tal",   clienteId, enderecoId, hoje, null, EstadoPedido.NOVO,                "45.00");
                inserir(conn, 1002, "Maria Souza",      clienteId, enderecoId, hoje, null, EstadoPedido.NOVO,                "62.50");
                inserir(conn, 1003, "Joao Pereira",     clienteId, enderecoId, hoje, null, EstadoPedido.AGUARDANDO_PAGAMENTO,"118.90");
                inserir(conn, 1004, "Ana Lima",         clienteId, enderecoId, hoje, null, EstadoPedido.EM_PREPARO,          "37.00");
                inserir(conn, 1005, "Carlos Dias",      clienteId, enderecoId, hoje, null, EstadoPedido.AGUARDANDO_ENTREGA,  "89.30");
                inserir(conn, 1006, "Beatriz Alves",    clienteId, enderecoId, hoje, null, EstadoPedido.EM_TRANSITO,         "140.30");
                inserir(conn, 1007, "Diego Castro",     clienteId, enderecoId, hoje, hoje, EstadoPedido.ENTREGUE,            "75.80");
                inserir(conn, 1008, "Elaine Rocha",     clienteId, enderecoId, hoje, hoje, EstadoPedido.ENTREGUE,            "54.20");
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            System.err.println("AVISO: falha ao semear dados demo - " + e.getMessage());
        }
    }

    private static int obterOuCriarClienteDemo(Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM clientes WHERE cpf = ?")) {
            ps.setString(1, "00000000000");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO clientes (nome, cpf) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "Cliente Demo");
            ps.setString(2, "00000000000");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new Exception("Falha ao criar cliente demo");
    }

    private static int obterOuCriarEnderecoDemo(Connection conn, int clienteId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM enderecos WHERE cliente_id = ? LIMIT 1")) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO enderecos (cliente_id, logradouro, numero, bairro, cidade, uf, cep, padrao) VALUES (?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, clienteId);
            ps.setString(2, "Rua Demo");
            ps.setString(3, "1");
            ps.setString(4, "Centro");
            ps.setString(5, "Vitoria");
            ps.setString(6, "ES");
            ps.setString(7, "29000000");
            ps.setInt(8, 1);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new Exception("Falha ao criar endereco demo");
    }

    private static void inserir(Connection conn, int codigo, String clienteNome,
                                int clienteId, int enderecoId,
                                LocalDate dataPedido, LocalDate dataConclusao,
                                EstadoPedido estado, String valor) throws Exception {
        String sql = """
            INSERT INTO pedidos
                (codigo, cliente_id, cliente_nome, endereco_id, data_pedido,
                 data_conclusao, estado, valor_total)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, codigo);
            ps.setInt(2, clienteId);
            ps.setString(3, clienteNome);
            ps.setInt(4, enderecoId);
            ps.setString(5, dataPedido.toString());
            ps.setString(6, dataConclusao == null ? null : dataConclusao.toString());
            ps.setString(7, estado.getDescricao());
            ps.setString(8, valor);
            ps.executeUpdate();
        }
    }
}
