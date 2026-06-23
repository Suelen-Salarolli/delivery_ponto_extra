package com.ufes.delivery.service;

import com.ufes.delivery.auditoria.IAuditoriaService;
import com.ufes.delivery.dao.ClienteDAO;
import com.ufes.delivery.model.Sessao;
import com.ufes.delivery.model.cadastro.Cliente;
import com.ufes.delivery.util.CpfUtil;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Regras de negocio de cliente (US05/US06): cadastro/edicao com CPF unico,
 * invariantes de endereco (1..3 com um padrao) e busca por Nome ou CPF.
 */
public class ClienteService {

    public enum AtributoBusca { NOME, CPF }

    private final ClienteDAO clienteDAO;
    private final IAuditoriaService auditoria;

    public ClienteService(ClienteDAO clienteDAO, IAuditoriaService auditoria) {
        this.clienteDAO = clienteDAO;
        this.auditoria = auditoria;
    }

    /** US06 — cadastra cliente novo com seus enderecos em transacao unica. */
    public Cliente cadastrar(Cliente cliente) throws SQLException {
        cliente.validarEnderecos();

        if (clienteDAO.existeCpf(cliente.getCpf())) {
            auditoria.registrar(Sessao.getInstance().getUsernameAtual(),
                "CADASTRO_CLIENTE", "cliente:" + cliente.getCpfFormatado(),
                "Rejeitado", "CPF ja vinculado a cliente existente");
            throw new IllegalArgumentException("CPF ja esta vinculado a cliente existente");
        }

        clienteDAO.salvar(cliente);
        auditoria.registrar(Sessao.getInstance().getUsernameAtual(),
            "CADASTRO_CLIENTE", "cliente:" + cliente.getCpfFormatado(),
            "Sucesso", "Nome: " + cliente.getNome());
        return cliente;
    }

    /** US06 — atualiza cliente existente (CPF permanece unico no cadastro). */
    public void atualizar(Cliente cliente) throws SQLException {
        cliente.validarEnderecos();

        if (clienteDAO.existeCpf(cliente.getCpf(), cliente.getId())) {
            throw new IllegalArgumentException("CPF ja esta vinculado a cliente existente");
        }

        clienteDAO.atualizar(cliente);
        auditoria.registrar(Sessao.getInstance().getUsernameAtual(),
            "ALTERACAO_CLIENTE", "cliente:" + cliente.getCpfFormatado(),
            "Sucesso", "Nome: " + cliente.getNome());
    }

    /** US05 — busca por atributo. CPF e validado pelos digitos verificadores antes da consulta. */
    public List<Cliente> buscar(AtributoBusca atributo, String valor) throws SQLException {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("Valor da busca e obrigatorio");

        return switch (atributo) {
            case NOME -> clienteDAO.buscarPorNome(valor.trim());
            case CPF -> {
                if (!CpfUtil.isValido(valor))
                    throw new IllegalArgumentException("CPF invalido");
                yield clienteDAO.buscarPorCpf(CpfUtil.normalizar(valor));
            }
        };
    }

    public List<Cliente> listarTodos() throws SQLException {
        return clienteDAO.listarTodos();
    }

    public Optional<Cliente> buscarPorId(int id) throws SQLException {
        return clienteDAO.buscarPorId(id);
    }
}
