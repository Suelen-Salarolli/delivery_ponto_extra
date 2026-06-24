package com.ufes.delivery.model.pedido;

import com.ufes.delivery.model.cadastro.Categoria;
import com.ufes.delivery.model.cadastro.Produto;
import java.math.BigDecimal;

public class PedidoItem {

    private final int produtoId;
    private final int produtoCodigo;
    private final String produtoNome;
    private final Categoria categoria;
    private int quantidade;
    private final BigDecimal precoUnitario;

    public PedidoItem(Produto produto, int quantidade) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto e obrigatorio");
        }
        this.produtoId = produto.getId();
        this.produtoCodigo = produto.getCodigo();
        this.produtoNome = produto.getNome();
        this.categoria = produto.getCategoria();
        this.precoUnitario = produto.getPrecoUnitario();
        setQuantidade(quantidade);
    }

    public int getProdutoId() { return produtoId; }
    public int getProdutoCodigo() { return produtoCodigo; }
    public String getProdutoNome() { return produtoNome; }
    public Categoria getCategoria() { return categoria; }
    public int getQuantidade() { return quantidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }

    public void setQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        this.quantidade = quantidade;
    }

    public BigDecimal getSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
