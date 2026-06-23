package com.ufes.delivery.model.cadastro;

/**
 * Endereco de entrega de um cliente (US06).
 * Logradouro, numero, bairro e cidade obrigatorios; complemento opcional;
 * UF sigla valida; CEP oito digitos (com ou sem mascara).
 */
public class Endereco {

    private int id;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private Uf uf;
    private String cep;        // armazenado normalizado (8 digitos)
    private boolean padrao;

    public Endereco() {}

    public Endereco(String logradouro, String numero, String complemento,
                    String bairro, String cidade, Uf uf, String cep, boolean padrao) {
        setLogradouro(logradouro);
        setNumero(numero);
        setComplemento(complemento);
        setBairro(bairro);
        setCidade(cidade);
        setUf(uf);
        setCep(cep);
        this.padrao = padrao;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) {
        this.logradouro = obrigatorio(logradouro, "Logradouro e obrigatorio");
    }

    public String getNumero() { return numero; }
    public void setNumero(String numero) {
        this.numero = obrigatorio(numero, "Numero e obrigatorio");
    }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) {
        this.complemento = (complemento == null) ? null : complemento.trim();
    }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) {
        this.bairro = obrigatorio(bairro, "Bairro e obrigatorio");
    }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) {
        this.cidade = obrigatorio(cidade, "Cidade e obrigatoria");
    }

    public Uf getUf() { return uf; }
    public void setUf(Uf uf) {
        if (uf == null)
            throw new IllegalArgumentException("UF e obrigatoria");
        this.uf = uf;
    }

    public String getCep() { return cep; }
    public void setCep(String cep) {
        String d = (cep == null) ? "" : cep.replaceAll("\\D", "");
        if (d.length() != 8)
            throw new IllegalArgumentException("CEP deve conter oito digitos");
        this.cep = d;
    }

    /** CEP formatado 00000-000. */
    public String getCepFormatado() {
        if (cep == null || cep.length() != 8) return cep;
        return cep.substring(0, 5) + "-" + cep.substring(5);
    }

    public boolean isPadrao() { return padrao; }
    public void setPadrao(boolean padrao) { this.padrao = padrao; }

    private String obrigatorio(String valor, String msg) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException(msg);
        return valor.trim();
    }

    @Override
    public String toString() {
        return logradouro + ", " + numero + " - " + bairro + ", " + cidade + "/" + uf
                + " - " + getCepFormatado() + (padrao ? " (Padrao)" : "");
    }
}
