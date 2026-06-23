package com.ufes.delivery.service;

import com.ufes.delivery.auditoria.IAuditoriaService;
import com.ufes.delivery.dao.ProdutoDAO;
import com.ufes.delivery.model.Sessao;
import com.ufes.delivery.model.cadastro.Categoria;
import com.ufes.delivery.model.cadastro.Produto;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Regras de negocio de produto (US07). Cadastro com codigo unico, categoria
 * por lista controlada, preco e estoque validados; busca por atributo.
 */
public class ProdutoService {

    public enum AtributoBusca { CODIGO, NOME, CATEGORIA }

    private final ProdutoDAO produtoDAO;
    private final IAuditoriaService auditoria;

    public ProdutoService(ProdutoDAO produtoDAO, IAuditoriaService auditoria) {
        this.produtoDAO = produtoDAO;
        this.auditoria = auditoria;
    }

    /** US07 — cadastra novo produto. Codigo deve ser unico no catalogo. */
    public Produto cadastrar(int codigo, String nome, Categoria categoria,
                             BigDecimal preco, int estoque) throws SQLException {
        Produto p = new Produto(codigo, nome, categoria, preco, estoque);

        if (produtoDAO.existeCodigo(codigo)) {
            auditoria.registrar(Sessao.getInstance().getUsernameAtual(),
                "CADASTRO_PRODUTO", "produto:" + codigo,
                "Rejeitado", "Codigo ja esta em uso");
            throw new IllegalArgumentException("Codigo ja esta em uso");
        }

        produtoDAO.salvar(p);
        auditoria.registrar(Sessao.getInstance().getUsernameAtual(),
            "CADASTRO_PRODUTO", "produto:" + codigo,
            "Sucesso", "Categoria: " + categoria.getDescricao());
        return p;
    }

    /** US07 — atualiza produto existente (codigo nao muda). */
    public void atualizar(Produto produto) throws SQLException {
        produtoDAO.atualizar(produto);
        auditoria.registrar(Sessao.getInstance().getUsernameAtual(),
            "ALTERACAO_PRODUTO", "produto:" + produto.getCodigo(),
            "Sucesso", null);
    }

    /** US07 — busca por atributo selecionado (Codigo, Nome ou Categoria). */
    public List<Produto> buscar(AtributoBusca atributo, String valor) throws SQLException {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("Valor da busca e obrigatorio");
        String v = valor.trim();
        return switch (atributo) {
            case CODIGO -> {
                int cod;
                try {
                    cod = Integer.parseInt(v);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Codigo deve ser um numero inteiro");
                }
                yield produtoDAO.buscarPorCodigo(cod);
            }
            case NOME -> produtoDAO.buscarPorNome(v);
            case CATEGORIA -> produtoDAO.buscarPorCategoria(Categoria.fromDescricao(v).getDescricao());
        };
    }

    public List<Produto> listarTodos() throws SQLException {
        return produtoDAO.listarTodos();
    }

    public Optional<Produto> buscarPorId(int id) throws SQLException {
        return produtoDAO.buscarPorId(id);
    }
}
