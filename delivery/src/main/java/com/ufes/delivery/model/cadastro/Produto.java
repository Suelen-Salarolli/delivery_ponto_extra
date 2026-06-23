package com.ufes.delivery.model.cadastro;

import java.math.BigDecimal;

/**
 * Produto do catalogo (US07).
 *
 * AIDEV-NOTE: preco usa BigDecimal para honrar a regra transversal de dados
 * monetarios ("representacao decimal para impedir erro de arredondamento").
 * O dominio legado de descontos usa double; este e o modelo persistente novo.
 */
public class Produto {

    private int id;
    private int codigo;
    private String nome;
    private Categoria categoria;
    private BigDecimal precoUnitario;
    private int estoqueAtual;

    public Produto() {}

    public Produto(int codigo, String nome, Categoria categoria,
                   BigDecimal precoUnitario, int estoqueAtual) {
        setCodigo(codigo);
        setNome(nome);
        setCategoria(categoria);
        setPrecoUnitario(precoUnitario);
        setEstoqueAtual(estoqueAtual);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCodigo() { return codigo; }
    public void setCodigo(int codigo) {
        if (codigo <= 0)
            throw new IllegalArgumentException("Codigo deve ser um inteiro positivo");
        this.codigo = codigo;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome do produto e obrigatorio");
        String limpo = nome.trim();
        if (limpo.length() < 2 || limpo.length() > 120)
            throw new IllegalArgumentException("Nome do produto deve ter entre 2 e 120 caracteres");
        this.nome = limpo;
    }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) {
        if (categoria == null)
            throw new IllegalArgumentException("Categoria do produto e obrigatoria");
        this.categoria = categoria;
    }

    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(BigDecimal precoUnitario) {
        if (precoUnitario == null)
            throw new IllegalArgumentException("Preco unitario e obrigatorio");
        if (precoUnitario.signum() <= 0)
            throw new IllegalArgumentException("Preco unitario deve ser maior que R$ 0,00");
        if (precoUnitario.scale() > 2)
            throw new IllegalArgumentException("Preco unitario deve ter no maximo duas casas decimais");
        this.precoUnitario = precoUnitario;
    }

    public int getEstoqueAtual() { return estoqueAtual; }
    public void setEstoqueAtual(int estoqueAtual) {
        if (estoqueAtual < 0)
            throw new IllegalArgumentException("Quantidade em estoque deve ser inteira e maior ou igual a zero");
        this.estoqueAtual = estoqueAtual;
    }

    @Override
    public String toString() {
        return "Produto{codigo=" + codigo + ", nome='" + nome + "', categoria=" + categoria
                + ", preco=" + precoUnitario + ", estoque=" + estoqueAtual + "}";
    }
}
