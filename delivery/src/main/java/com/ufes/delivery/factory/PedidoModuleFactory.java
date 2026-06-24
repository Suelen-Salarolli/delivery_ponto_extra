package com.ufes.delivery.factory;

import com.ufes.delivery.auditoria.IAuditoriaService;
import com.ufes.delivery.dao.PagamentoDAO;
import com.ufes.delivery.dao.PedidoDAO;
import com.ufes.delivery.repository.CupomRepositoryEmMemoria;
import com.ufes.delivery.service.PagamentoService;
import com.ufes.delivery.service.PedidoService;
import com.ufes.delivery.service.SimuladorPagamentoAleatorio;

/**
 * Factory para o modulo de pedido + pagamento (US09/US10/US11).
 * Centraliza a composicao das dependencias para nao poluir o presenter do painel.
 *
 * AIDEV-NOTE: em testes, substituir SimuladorPagamentoAleatorio por
 * um mock deterministico via ISimuladorPagamento.
 */
public final class PedidoModuleFactory {

    private PedidoModuleFactory() {}

    public static PedidoService criarPedidoService(IAuditoriaService auditoria) {
        return new PedidoService(
            new PedidoDAO(),
            new CupomRepositoryEmMemoria(),
            auditoria
        );
    }

    public static PagamentoService criarPagamentoService(IAuditoriaService auditoria) {
        return new PagamentoService(
            new PagamentoDAO(),
            new SimuladorPagamentoAleatorio(),
            auditoria
        );
    }
}
