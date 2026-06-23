package com.ufes.delivery.model.cadastro;

import com.ufes.delivery.util.CpfUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cliente persistente do sistema (US05/US06): nome, CPF e ate tres enderecos
 * de entrega, com exatamente um marcado como padrao.
 *
 * AIDEV-NOTE: nao confundir com com.ufes.delivery.model.Cliente (legado), que
 * pertence ao demo de descontos do baseline CR2 e usa tipo/fidelidade/endereco unico.
 * A convergencia dos dois modelos ocorre na Fase 3/4 (US09 — reconstrucao do pedido).
 */
public class Cliente {

    public static final int MAX_ENDERECOS = 3;

    private int id;
    private String nome;
    private String cpf;   // normalizado (11 digitos)
    private final List<Endereco> enderecos = new ArrayList<>();

    public Cliente() {}

    public Cliente(String nome, String cpf) {
        setNome(nome);
        setCpf(cpf);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome do cliente e obrigatorio");
        String limpo = nome.trim();
        if (limpo.length() < 2 || limpo.length() > 120)
            throw new IllegalArgumentException("Nome deve ter entre 2 e 120 caracteres");
        if (!limpo.matches("[\\p{L} '\\-]+"))
            throw new IllegalArgumentException("Nome aceita apenas letras, espacos, apostrofos e hifens");
        this.nome = limpo;
    }

    public String getCpf() { return cpf; }
    public String getCpfFormatado() { return CpfUtil.formatar(cpf); }
    public void setCpf(String cpf) {
        if (!CpfUtil.isValido(cpf))
            throw new IllegalArgumentException("CPF invalido");
        this.cpf = CpfUtil.normalizar(cpf);
    }

    public List<Endereco> getEnderecos() {
        return Collections.unmodifiableList(enderecos);
    }

    public void adicionarEndereco(Endereco endereco) {
        if (endereco == null)
            throw new IllegalArgumentException("Endereco nao pode ser nulo");
        if (enderecos.size() >= MAX_ENDERECOS)
            throw new IllegalStateException("Cliente pode ter no maximo " + MAX_ENDERECOS + " enderecos");
        enderecos.add(endereco);
    }

    public void limparEnderecos() {
        enderecos.clear();
    }

    /**
     * Valida as invariantes de endereco do cliente (US06):
     * pelo menos um e no maximo tres enderecos, com exatamente um padrao.
     */
    public void validarEnderecos() {
        if (enderecos.isEmpty())
            throw new IllegalArgumentException("Cliente deve ter ao menos um endereco de entrega");
        if (enderecos.size() > MAX_ENDERECOS)
            throw new IllegalArgumentException("Cliente pode ter no maximo " + MAX_ENDERECOS + " enderecos");
        long padroes = enderecos.stream().filter(Endereco::isPadrao).count();
        if (padroes != 1)
            throw new IllegalArgumentException("Deve existir exatamente um endereco padrao");
    }

    public Endereco getEnderecoPadrao() {
        return enderecos.stream().filter(Endereco::isPadrao).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return "Cliente{id=" + id + ", nome='" + nome + "', cpf=" + getCpfFormatado()
                + ", enderecos=" + enderecos.size() + "}";
    }
}
