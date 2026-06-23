package com.ufes.delivery.service;

import com.ufes.delivery.auditoria.IAuditoriaService;
import com.ufes.delivery.configuracao.ConfiguracaoService;
import com.ufes.delivery.dao.MovimentacaoEstoqueDAO;
import com.ufes.delivery.model.Sessao;
import com.ufes.delivery.model.cadastro.MovimentacaoEstoque;
import com.ufes.delivery.model.cadastro.Produto;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Regras de negocio de movimentacao de estoque (US08).
 *
 * Tipos validos: Entrada (exige nota fiscal) e Ajuste de estoque (exige motivo).
 * O tipo Saida nao existe aqui — baixa por venda e responsabilidade do PagamentoService.
 *
 * A previa e calculada no presenter sem persistir. A confirmacao e atomica:
 * atualiza estoque_atual do produto e insere a movimentacao em transacao unica.
 */
public class EstoqueService {

    private final MovimentacaoEstoqueDAO movimentacaoDAO;
    private final IAuditoriaService auditoria;

    public EstoqueService(MovimentacaoEstoqueDAO movimentacaoDAO, IAuditoriaService auditoria) {
        this.movimentacaoDAO = movimentacaoDAO;
        this.auditoria = auditoria;
    }

    /**
     * Calcula a previa do estoque apos a movimentacao, SEM persistir.
     * Valida restricoes que independem do tipo (quantidade != 0, data, estoque >= 0).
     */
    public int calcularPrevia(Produto produto, int quantidade, LocalDate dataMovimentacao) {
        if (dataMovimentacao == null)
            throw new IllegalArgumentException("Data da movimentacao e obrigatoria");
        if (dataMovimentacao.isAfter(ConfiguracaoService.getDataOperacao()))
            throw new IllegalArgumentException(
                "Data da movimentacao nao pode ser posterior a data operacional vigente ("
                + ConfiguracaoService.getDataOperacao() + ")");
        if (quantidade == 0)
            throw new IllegalArgumentException("Quantidade a movimentar deve ser diferente de zero");

        int previa = produto.getEstoqueAtual() + quantidade;
        if (previa < 0)
            throw new IllegalArgumentException(
                "Estoque resultante seria negativo. Disponivel: " + produto.getEstoqueAtual());
        return previa;
    }

    /**
     * Confirma a movimentacao validando todas as regras de negocio (US08).
     * Atualiza o produto em memoria apos persistencia bem-sucedida.
     */
    public void confirmar(Produto produto, int quantidade, String tipo,
                          LocalDate dataMovimentacao, String motivo,
                          String notaFiscal) throws SQLException {

        // Valida restricoes comuns
        int previa = calcularPrevia(produto, quantidade, dataMovimentacao);

        // Restricoes por tipo
        if (MovimentacaoEstoque.TIPO_AJUSTE.equals(tipo)) {
            if (motivo == null || motivo.isBlank())
                throw new IllegalArgumentException("Motivo do ajuste e obrigatorio");
        } else if (MovimentacaoEstoque.TIPO_ENTRADA.equals(tipo)) {
            if (notaFiscal == null || notaFiscal.isBlank())
                throw new IllegalArgumentException("Numero da nota fiscal de entrada e obrigatorio");
            if (quantidade <= 0)
                throw new IllegalArgumentException("Quantidade de entrada deve ser positiva");
        } else {
            throw new IllegalArgumentException("Tipo invalido: use Entrada ou Ajuste de estoque");
        }

        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setProdutoId(produto.getId());
        mov.setProdutoNome(produto.getNome());
        mov.setTipo(tipo);
        mov.setQuantidade(quantidade);
        mov.setEstoqueAnterior(produto.getEstoqueAtual());
        mov.setEstoquePosterior(previa);
        mov.setDataMovimentacao(dataMovimentacao);
        mov.setMotivo(motivo != null && !motivo.isBlank() ? motivo : null);
        mov.setNotaFiscal(notaFiscal != null && !notaFiscal.isBlank() ? notaFiscal : null);
        mov.setUsuario(Sessao.getInstance().getUsernameAtual());
        mov.setDataHoraRegistro(LocalDateTime.now());

        // Transacao atomica: atualiza estoque + insere movimentacao
        movimentacaoDAO.confirmar(mov);

        // Atualiza o objeto em memoria para refletir o novo estoque
        produto.setEstoqueAtual(previa);

        // Auditoria
        String ref = MovimentacaoEstoque.TIPO_ENTRADA.equals(tipo)
            ? "NF:" + notaFiscal : "Motivo:" + motivo;
        auditoria.registrar(
            Sessao.getInstance().getUsernameAtual(),
            "MOVIMENTACAO_ESTOQUE",
            "produto:" + produto.getCodigo() + " (" + produto.getNome() + ")",
            "Confirmado",
            tipo + " | Qtd:" + quantidade + " | " + ref
        );
    }
}
