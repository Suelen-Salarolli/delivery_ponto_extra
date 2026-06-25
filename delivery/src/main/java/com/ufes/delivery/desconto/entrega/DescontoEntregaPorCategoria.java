package com.ufes.delivery.desconto.entrega;

import com.ufes.delivery.model.cadastro.Categoria;
import com.ufes.delivery.model.pedido.PedidoCadastro;
import com.ufes.delivery.model.pedido.PedidoItem;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Desconto na taxa de entrega conforme as categorias de item presentes no pedido.
 * Cada categoria contribui no maximo uma vez (nao acumula por item repetido).
 */
public class DescontoEntregaPorCategoria implements DescontoEntregaStrategy {

    private final Map<Categoria, BigDecimal> descontoPorCategoria = new EnumMap<>(Categoria.class);

    public DescontoEntregaPorCategoria() {
        descontoPorCategoria.put(Categoria.ALIMENTACAO, new BigDecimal("5.00"));
        descontoPorCategoria.put(Categoria.EDUCACAO, new BigDecimal("2.00"));
        descontoPorCategoria.put(Categoria.LAZER, new BigDecimal("1.50"));
    }

    @Override
    public boolean seAplica(PedidoCadastro pedido) {
        return pedido.getItens().stream()
            .anyMatch(item -> descontoPorCategoria.containsKey(item.getCategoria()));
    }

    @Override
    public BigDecimal calcular(PedidoCadastro pedido) {
        Set<Categoria> consideradas = EnumSet.noneOf(Categoria.class);
        BigDecimal total = BigDecimal.ZERO;
        for (PedidoItem item : pedido.getItens()) {
            Categoria categoria = item.getCategoria();
            if (descontoPorCategoria.containsKey(categoria) && consideradas.add(categoria)) {
                total = total.add(descontoPorCategoria.get(categoria));
            }
        }
        return total;
    }

    @Override
    public String nome() {
        return "Desconto entrega por categoria de item";
    }
}
