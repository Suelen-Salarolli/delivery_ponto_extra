package com.ufes.delivery.model.cadastro;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representa uma movimentacao manual de estoque (US08).
 * Tipos validos: Entrada (exige nota fiscal) e Ajuste de estoque (exige motivo).
 * Saida automatica por venda NAO passa por aqui — ocorre em transacao de pagamento.
 */
public class MovimentacaoEstoque {

    public static final String TIPO_ENTRADA = "Entrada";
    public static final String TIPO_AJUSTE  = "Ajuste de estoque";

    private int id;
    private int produtoId;
    private String produtoNome;
    private String tipo;
    private int quantidade;          // pode ser negativo em ajuste que reduz
    private int estoqueAnterior;
    private int estoquePosterior;
    private LocalDate dataMovimentacao;
    private String motivo;           // obrigatorio para Ajuste
    private String notaFiscal;       // obrigatorio para Entrada
    private String usuario;
    private LocalDateTime dataHoraRegistro;

    public MovimentacaoEstoque() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProdutoId() { return produtoId; }
    public void setProdutoId(int produtoId) { this.produtoId = produtoId; }

    public String getProdutoNome() { return produtoNome; }
    public void setProdutoNome(String produtoNome) { this.produtoNome = produtoNome; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) {
        if (!TIPO_ENTRADA.equals(tipo) && !TIPO_AJUSTE.equals(tipo))
            throw new IllegalArgumentException("Tipo invalido: use Entrada ou Ajuste de estoque");
        this.tipo = tipo;
    }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) {
        if (quantidade == 0)
            throw new IllegalArgumentException("Quantidade a movimentar deve ser diferente de zero");
        this.quantidade = quantidade;
    }

    public int getEstoqueAnterior() { return estoqueAnterior; }
    public void setEstoqueAnterior(int estoqueAnterior) { this.estoqueAnterior = estoqueAnterior; }

    public int getEstoquePosterior() { return estoquePosterior; }
    public void setEstoquePosterior(int estoquePosterior) { this.estoquePosterior = estoquePosterior; }

    public LocalDate getDataMovimentacao() { return dataMovimentacao; }
    public void setDataMovimentacao(LocalDate dataMovimentacao) {
        if (dataMovimentacao == null)
            throw new IllegalArgumentException("Data da movimentacao e obrigatoria");
        this.dataMovimentacao = dataMovimentacao;
    }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getNotaFiscal() { return notaFiscal; }
    public void setNotaFiscal(String notaFiscal) { this.notaFiscal = notaFiscal; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public LocalDateTime getDataHoraRegistro() { return dataHoraRegistro; }
    public void setDataHoraRegistro(LocalDateTime dataHoraRegistro) { this.dataHoraRegistro = dataHoraRegistro; }

    /** Calcula previa do estoque: anterior + quantidade (quantidade negativa reduz). */
    public int calcularPrevia() {
        return estoqueAnterior + quantidade;
    }
}
