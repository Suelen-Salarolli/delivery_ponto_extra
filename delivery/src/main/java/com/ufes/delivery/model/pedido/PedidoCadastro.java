package com.ufes.delivery.model.pedido;

import com.ufes.delivery.model.cadastro.Cliente;
import com.ufes.delivery.model.cadastro.Endereco;
import com.ufes.delivery.model.cadastro.EstadoPedido;
import com.ufes.delivery.model.pedido.estado.EstadoPedidoState;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PedidoCadastro {

    private int id;
    private int codigo;
    private Cliente cliente;
    private Endereco endereco;
    private LocalDate dataPedido;
    private EstadoPedido estado = EstadoPedido.NOVO;
    private String cupomCodigo;
    private BigDecimal descontoItens = BigDecimal.ZERO;
    private BigDecimal descontoEntrega = BigDecimal.ZERO;
    private BigDecimal taxaEntrega = BigDecimal.ZERO;
    private BigDecimal valorTotal = BigDecimal.ZERO;
    private final List<PedidoItem> itens = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCodigo() { return codigo; }
    public void setCodigo(int codigo) { this.codigo = codigo; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }

    public LocalDate getDataPedido() { return dataPedido; }
    public void setDataPedido(LocalDate dataPedido) { this.dataPedido = dataPedido; }

    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }

    /**
     * Transiciona o pedido apos pagamento aprovado, delegando a regra ao padrao
     * State. Lanca IllegalStateException se o estado atual nao admitir a aprovacao
     * (ex.: pedido ja entregue).
     */
    public void aprovarPagamento() {
        this.estado = EstadoPedidoState.de(estado).aprovarPagamento().valor();
    }

    public String getCupomCodigo() { return cupomCodigo; }
    public void setCupomCodigo(String cupomCodigo) { this.cupomCodigo = cupomCodigo; }

    public BigDecimal getDescontoItens() { return descontoItens; }
    public void setDescontoItens(BigDecimal descontoItens) { this.descontoItens = descontoItens; }

    public BigDecimal getDescontoEntrega() { return descontoEntrega; }
    public void setDescontoEntrega(BigDecimal descontoEntrega) { this.descontoEntrega = descontoEntrega; }

    public BigDecimal getTaxaEntrega() { return taxaEntrega; }
    public void setTaxaEntrega(BigDecimal taxaEntrega) { this.taxaEntrega = taxaEntrega; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public List<PedidoItem> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public void adicionarItem(PedidoItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Item do pedido e obrigatorio");
        }
        itens.add(item);
    }

    public void removerItem(int indice) {
        itens.remove(indice);
    }

    public BigDecimal getSubtotalItens() {
        return itens.stream()
            .map(PedidoItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Taxa de entrega final = taxa base menos o desconto de entrega, nunca negativa
     * (regra transversal: desconto na taxa de entrega nao gera taxa final negativa).
     */
    public BigDecimal getTaxaEntregaFinal() {
        BigDecimal taxaFinal = taxaEntrega.subtract(descontoEntrega);
        return taxaFinal.signum() < 0 ? BigDecimal.ZERO.setScale(2) : taxaFinal.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Recalcula o total do pedido a partir do proprio estado (modelo rico):
     * subtotal dos itens - desconto de itens + taxa de entrega final.
     * O total nunca e negativo.
     *
     * AIDEV-NOTE: o calculo vive no agregado (e nao no servico) para manter a
     * regra de total coesa com os dados que a determinam.
     */
    public void recalcularTotais() {
        BigDecimal total = getSubtotalItens()
            .subtract(descontoItens)
            .add(getTaxaEntregaFinal())
            .setScale(2, RoundingMode.HALF_UP);
        if (total.signum() < 0) {
            total = BigDecimal.ZERO.setScale(2);
        }
        this.valorTotal = total;
    }
}
