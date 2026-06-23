package com.ufes.delivery.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexaoDB {

    private static final String URL = "jdbc:sqlite:delivery.db";

    private ConexaoDB() {}

    /**
     * Abre uma conexao nova e independente. Cada chamador e responsavel por
     * fechar a conexao recebida (use try-with-resources).
     *
     * AIDEV-NOTE: abandonamos o singleton de conexao porque DAOs que precisam
     * de transacao chamavam setAutoCommit(false) na conexao global, criando
     * risco de corrupcao de estado em operacoes concorrentes ou aninhadas.
     * SQLite serializa escritas por arquivo, entao multiplas conexoes sao seguras.
     */
    public static Connection getConexao() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        conn.createStatement().execute("PRAGMA foreign_keys = ON");
        conn.createStatement().execute("PRAGMA journal_mode = WAL");
        return conn;
    }

    public static void inicializarBanco() throws SQLException {
        try (Connection conn = getConexao(); Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS usuarios (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL,
                    username TEXT NOT NULL UNIQUE,
                    senha_hash TEXT NOT NULL,
                    perfil TEXT NOT NULL CHECK(perfil IN ('Administrador','Atendente')),
                    situacao TEXT NOT NULL CHECK(situacao IN ('Autorizado','Pendente','Nao autorizado'))
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS auditoria (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    usuario TEXT NOT NULL,
                    data_hora TEXT NOT NULL,
                    operacao TEXT NOT NULL,
                    recurso TEXT NOT NULL,
                    resultado TEXT NOT NULL,
                    justificativa TEXT
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS produtos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    codigo INTEGER NOT NULL UNIQUE,
                    nome TEXT NOT NULL,
                    categoria TEXT NOT NULL,
                    preco_unitario TEXT NOT NULL,
                    estoque_atual INTEGER NOT NULL CHECK(estoque_atual >= 0)
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS clientes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL,
                    cpf TEXT NOT NULL UNIQUE
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS enderecos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    cliente_id INTEGER NOT NULL,
                    logradouro TEXT NOT NULL,
                    numero TEXT NOT NULL,
                    complemento TEXT,
                    bairro TEXT NOT NULL,
                    cidade TEXT NOT NULL,
                    uf TEXT NOT NULL,
                    cep TEXT NOT NULL,
                    padrao INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE
                )
            """);

            // US08 — movimentacoes de estoque (Entrada e Ajuste; Saida apenas automatica)
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS estoque_movimentacoes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    produto_id INTEGER NOT NULL,
                    tipo TEXT NOT NULL CHECK(tipo IN ('Entrada','Ajuste de estoque')),
                    quantidade INTEGER NOT NULL,
                    estoque_anterior INTEGER NOT NULL,
                    estoque_posterior INTEGER NOT NULL,
                    data_movimentacao TEXT NOT NULL,
                    motivo TEXT,
                    nota_fiscal TEXT,
                    usuario TEXT NOT NULL,
                    data_hora_registro TEXT NOT NULL,
                    FOREIGN KEY (produto_id) REFERENCES produtos(id)
                )
            """);

            // US04/US09/US10 — pedidos completos
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS pedidos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    codigo INTEGER NOT NULL UNIQUE,
                    cliente_id INTEGER NOT NULL,
                    cliente_nome TEXT NOT NULL,
                    endereco_id INTEGER NOT NULL,
                    cupom_codigo TEXT,
                    desconto_itens TEXT NOT NULL DEFAULT '0.00',
                    desconto_entrega TEXT NOT NULL DEFAULT '0.00',
                    taxa_entrega TEXT NOT NULL DEFAULT '0.00',
                    valor_total TEXT NOT NULL DEFAULT '0.00',
                    data_pedido TEXT NOT NULL,
                    data_conclusao TEXT,
                    estado TEXT NOT NULL DEFAULT 'Novo',
                    FOREIGN KEY (cliente_id) REFERENCES clientes(id),
                    FOREIGN KEY (endereco_id) REFERENCES enderecos(id)
                )
            """);

            // US09 — itens do pedido
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS pedido_itens (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    pedido_id INTEGER NOT NULL,
                    produto_id INTEGER NOT NULL,
                    produto_nome TEXT NOT NULL,
                    categoria TEXT NOT NULL,
                    quantidade INTEGER NOT NULL,
                    preco_unitario TEXT NOT NULL,
                    FOREIGN KEY (pedido_id) REFERENCES pedidos(id),
                    FOREIGN KEY (produto_id) REFERENCES produtos(id)
                )
            """);

            // US11 — tentativas de pagamento simulado
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS pagamentos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    pedido_id INTEGER NOT NULL,
                    forma_pagamento TEXT NOT NULL,
                    resultado TEXT NOT NULL CHECK(resultado IN ('Aprovado','Reprovado')),
                    identificador_transacao TEXT,
                    valor_pago TEXT,
                    prazo_estimado_entrega TEXT,
                    data_hora_tentativa TEXT NOT NULL,
                    FOREIGN KEY (pedido_id) REFERENCES pedidos(id)
                )
            """);
        }
    }
}
