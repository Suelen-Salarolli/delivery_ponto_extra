package com.ufes.delivery.db;

import com.ufes.delivery.configuracao.ConfiguracaoService;
import com.ufes.delivery.model.cadastro.EstadoPedido;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
            semearProdutosSeVazio(conn);

            conn.setAutoCommit(false);
            try {
                // Remove old demo orders (1001-1008) and their potential dependencies
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM pagamentos WHERE pedido_id IN (SELECT id FROM pedidos WHERE codigo BETWEEN 1001 AND 1008)")) {
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM pedido_itens WHERE pedido_id IN (SELECT id FROM pedidos WHERE codigo BETWEEN 1001 AND 1008)")) {
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM pedidos WHERE codigo BETWEEN 1001 AND 1008")) {
                    ps.executeUpdate();
                }

                // Cria cliente demo (se nao existir)
                int clienteId = obterOuCriarClienteDemo(conn);
                int enderecoId = obterOuCriarEnderecoDemo(conn, clienteId);

                LocalDateTime agora = LocalDateTime.now();
                int totalMinutos = agora.getHour() * 60 + agora.getMinute();
                LocalDate hoje = agora.toLocalDate();

                // Generate timestamps distributed up to the current elapsed minutes of the day (never in the future)
                String[] tempos = new String[8];
                for (int i = 0; i < 8; i++) {
                    int minutos = 0;
                    if (totalMinutos > 0) {
                        minutos = (int) (totalMinutos * (0.05 + 0.90 * (i / 7.0)));
                        if (minutos >= totalMinutos) {
                            minutos = totalMinutos - 1;
                        }
                        if (minutos < 0) {
                            minutos = 0;
                        }
                    }
                    LocalTime hora = LocalTime.of(minutos / 60, minutos % 60);
                    tempos[i] = hoje.toString() + " " + String.format("%02d:%02d:00", hora.getHour(), hora.getMinute());
                }

                inserir(conn, 1001, "Fulano de Tal",   clienteId, enderecoId, tempos[0], null, EstadoPedido.NOVO,                "45.00");
                inserir(conn, 1002, "Maria Souza",      clienteId, enderecoId, tempos[1], null, EstadoPedido.NOVO,                "62.50");
                inserir(conn, 1003, "Joao Pereira",     clienteId, enderecoId, tempos[2], null, EstadoPedido.AGUARDANDO_PAGAMENTO,"118.90");
                inserir(conn, 1004, "Ana Lima",         clienteId, enderecoId, tempos[3], null, EstadoPedido.EM_PREPARO,          "37.00");
                inserir(conn, 1005, "Carlos Dias",      clienteId, enderecoId, tempos[4], null, EstadoPedido.AGUARDANDO_ENTREGA,  "89.30");
                inserir(conn, 1006, "Beatriz Alves",    clienteId, enderecoId, tempos[5], null, EstadoPedido.EM_TRANSITO,         "140.30");
                // Diego and Elaine are Entregue, concluding at their creation time or slightly after
                inserir(conn, 1007, "Diego Castro",     clienteId, enderecoId, tempos[6], tempos[6], EstadoPedido.ENTREGUE,            "75.80");
                inserir(conn, 1008, "Elaine Rocha",     clienteId, enderecoId, tempos[7], tempos[7], EstadoPedido.ENTREGUE,            "54.20");
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            System.err.println("AVISO: falha ao semear dados demo - " + e.getMessage());
        }
    }

    private static void semearProdutosSeVazio(Connection conn) throws Exception {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM produtos")) {
            if (rs.next() && rs.getInt(1) > 0) return;
        }

        String sql = """
            INSERT INTO produtos (codigo, nome, categoria, preco_unitario, estoque_atual)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            inserirProdutoDemo(ps, 101, "Pizza Calabresa", "Alimentacao", "49.90", 20);
            inserirProdutoDemo(ps, 102, "Hamburguer Artesanal", "Alimentacao", "34.90", 18);
            inserirProdutoDemo(ps, 201, "Refrigerante 2L", "Alimentacao", "12.00", 30);
            inserirProdutoDemo(ps, 301, "Brownie", "Alimentacao", "14.00", 15);
            ps.executeBatch();
        }
    }

    private static void inserirProdutoDemo(PreparedStatement ps, int codigo, String nome,
                                           String categoria, String preco, int estoque) throws Exception {
        ps.setInt(1, codigo);
        ps.setString(2, nome);
        ps.setString(3, categoria);
        ps.setString(4, preco);
        ps.setInt(5, estoque);
        ps.addBatch();
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
                                String dataPedido, String dataConclusao,
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
            ps.setString(5, dataPedido);
            ps.setString(6, dataConclusao);
            ps.setString(7, estado.getDescricao());
            ps.setString(8, valor);
            ps.executeUpdate();
        }
    }
}
