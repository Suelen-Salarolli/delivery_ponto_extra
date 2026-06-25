package com.ufes.delivery.desconto.entrega;

import com.ufes.delivery.model.pedido.PedidoCadastro;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Desconto na taxa de entrega conforme o bairro do endereco de entrega.
 */
public class DescontoEntregaPorBairro implements DescontoEntregaStrategy {

    private final Map<String, BigDecimal> descontoPorBairro = new HashMap<>();

    public DescontoEntregaPorBairro() {
        descontoPorBairro.put("Centro", new BigDecimal("2.00"));
        descontoPorBairro.put("Bela Vista", new BigDecimal("3.00"));
        descontoPorBairro.put("Cidade Maravilhosa", new BigDecimal("1.50"));
    }

    @Override
    public boolean seAplica(PedidoCadastro pedido) {
        String bairro = bairro(pedido);
        return bairro != null && descontoPorBairro.containsKey(bairro);
    }

    @Override
    public BigDecimal calcular(PedidoCadastro pedido) {
        return descontoPorBairro.getOrDefault(bairro(pedido), BigDecimal.ZERO);
    }

    @Override
    public String nome() {
        return "Desconto entrega por bairro";
    }

    private String bairro(PedidoCadastro pedido) {
        return pedido.getEndereco() == null ? null : pedido.getEndereco().getBairro();
    }
}
