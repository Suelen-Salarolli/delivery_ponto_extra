package com.ufes.delivery.service;

import com.ufes.delivery.dao.PedidoResumoDAO;
import com.ufes.delivery.model.cadastro.EstadoPedido;
import com.ufes.delivery.model.cadastro.PedidoResumo;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Dados do painel operacional (US04): pedidos da data de operacao e metricas
 * por estado, coerentes com a lista exibida.
 */
public class PainelService {

    /** Metricas agregadas do painel. */
    public record Metricas(
        int pedidosDoDia,
        int novos,
        int aguardandoPagamento,
        int emPreparo,
        int aguardandoEntrega,
        int emTransito,
        int entreguesHoje
    ) {}

    private final PedidoResumoDAO pedidoDAO;

    public PainelService(PedidoResumoDAO pedidoDAO) {
        this.pedidoDAO = pedidoDAO;
    }

    public List<PedidoResumo> getPedidos(LocalDate dataOperacao) throws SQLException {
        return pedidoDAO.listarPorDataPedido(dataOperacao);
    }

    public Metricas getMetricas(LocalDate dataOperacao) throws SQLException {
        List<PedidoResumo> pedidos = pedidoDAO.listarPorDataPedido(dataOperacao);
        int entreguesHoje = pedidoDAO.contarEntreguesPorConclusao(dataOperacao);
        return new Metricas(
            pedidos.size(),
            contar(pedidos, EstadoPedido.NOVO),
            contar(pedidos, EstadoPedido.AGUARDANDO_PAGAMENTO),
            contar(pedidos, EstadoPedido.EM_PREPARO),
            contar(pedidos, EstadoPedido.AGUARDANDO_ENTREGA),
            contar(pedidos, EstadoPedido.EM_TRANSITO),
            entreguesHoje
        );
    }

    private int contar(List<PedidoResumo> pedidos, EstadoPedido estado) {
        return (int) pedidos.stream().filter(p -> p.getEstado() == estado).count();
    }
}
