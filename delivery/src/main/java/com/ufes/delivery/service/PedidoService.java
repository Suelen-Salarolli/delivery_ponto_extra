package com.ufes.delivery.service;

import com.ufes.delivery.auditoria.IAuditoriaService;
import com.ufes.delivery.configuracao.ConfiguracaoService;
import com.ufes.delivery.dao.PedidoDAO;
import com.ufes.delivery.desconto.entrega.CalculadoraDescontoEntregaService;
import com.ufes.delivery.model.CupomDescontoPedido;
import com.ufes.delivery.model.Sessao;
import com.ufes.delivery.model.cadastro.Cliente;
import com.ufes.delivery.model.cadastro.Endereco;
import com.ufes.delivery.model.cadastro.EstadoPedido;
import com.ufes.delivery.model.pedido.PedidoCadastro;
import com.ufes.delivery.model.pedido.PedidoItem;
import com.ufes.delivery.repository.ICupomRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class PedidoService {

    private final PedidoDAO pedidoDAO;
    private final ICupomRepository cupomRepository;
    private final CalculadoraDescontoEntregaService calculadoraEntrega;
    private final IAuditoriaService auditoria;

    public PedidoService(PedidoDAO pedidoDAO,
                         ICupomRepository cupomRepository,
                         CalculadoraDescontoEntregaService calculadoraEntrega,
                         IAuditoriaService auditoria) {
        this.pedidoDAO = pedidoDAO;
        this.cupomRepository = cupomRepository;
        this.calculadoraEntrega = calculadoraEntrega;
        this.auditoria = auditoria;
    }

    public PedidoCadastro montarPedido(Cliente cliente,
                                       Endereco endereco,
                                       List<PedidoItem> itens,
                                       String cupomCodigo,
                                       LocalDate dataPedido) throws SQLException {
        validarClienteEndereco(cliente, endereco);
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("Itens: informe ao menos um item para o pedido");
        }

        PedidoCadastro pedido = new PedidoCadastro();
        pedido.setCodigo(pedidoDAO.proximoCodigo());
        pedido.setCliente(cliente);
        pedido.setEndereco(endereco);
        pedido.setDataPedido(dataPedido == null ? LocalDate.now() : dataPedido);
        pedido.setEstado(EstadoPedido.NOVO);
        itens.forEach(pedido::adicionarItem);
        LocalDate data = pedido.getDataPedido();
        aplicarCupom(pedido, cupomCodigo, data.atTime(12, 0));
        aplicarTaxaEntrega(pedido);
        pedido.recalcularTotais();
        return pedido;
    }

    public PedidoCadastro salvarNovoPedido(Cliente cliente,
                                           Endereco endereco,
                                           List<PedidoItem> itens,
                                           String cupomCodigo,
                                           LocalDate dataPedido) throws SQLException {
        PedidoCadastro pedido = montarPedido(cliente, endereco, itens, cupomCodigo, dataPedido);
        pedidoDAO.salvar(pedido);
        auditoria.registrar(Sessao.getInstance().getUsernameAtual(),
            "CADASTRO_PEDIDO", "pedido:" + pedido.getCodigo(),
            "Sucesso", "Cliente: " + cliente.getNome());
        return pedido;
    }

    private void validarClienteEndereco(Cliente cliente, Endereco endereco) {
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente: selecione um cliente");
        }
        if (endereco == null) {
            throw new IllegalArgumentException("Endereco: selecione um endereco de entrega");
        }
        boolean pertenceAoCliente = cliente.getEnderecos().stream()
            .anyMatch(e -> e.getId() == endereco.getId());
        if (!pertenceAoCliente) {
            throw new IllegalArgumentException("Endereco: endereco nao pertence ao cliente selecionado");
        }
    }

    private void aplicarCupom(PedidoCadastro pedido, String cupomCodigo, LocalDateTime agora) {
        if (cupomCodigo == null || cupomCodigo.isBlank()) {
            pedido.setCupomCodigo(null);
            pedido.setDescontoItens(BigDecimal.ZERO);
            return;
        }

        String codigo = cupomCodigo.trim().toUpperCase();
        Optional<CupomDescontoPedido> encontrado = cupomRepository.buscarCupom(codigo);
        if (encontrado.isEmpty()) {
            throw new IllegalArgumentException("Cupom: cupom inexistente");
        }

        CupomDescontoPedido cupom = encontrado.get();
        if (agora.isBefore(cupom.getDataHoraInicio()) || agora.isAfter(cupom.getDataHoraFim())) {
            throw new IllegalArgumentException("Cupom: cupom fora do periodo de validade");
        }

        BigDecimal percentual = BigDecimal.valueOf(cupom.getPercentual())
            .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        BigDecimal desconto = pedido.getSubtotalItens().multiply(percentual)
            .setScale(2, RoundingMode.HALF_UP);
        pedido.setCupomCodigo(codigo);
        pedido.setDescontoItens(desconto);
    }

    /**
     * Aplica a taxa de entrega base e o desconto de entrega calculado pelas
     * estrategias (Strategy). O total e recalculado pelo proprio agregado.
     */
    private void aplicarTaxaEntrega(PedidoCadastro pedido) {
        BigDecimal taxaBase = BigDecimal.valueOf(ConfiguracaoService.getTaxaEntregaPadrao())
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal descontoEntrega = calculadoraEntrega.calcularDescontoEntrega(pedido, taxaBase);
        pedido.setTaxaEntrega(taxaBase);
        pedido.setDescontoEntrega(descontoEntrega);
    }
}
