package com.ufes.delivery.ui.pagamento;

import com.ufes.delivery.model.pagamento.ResultadoPagamento;
import com.ufes.delivery.model.pedido.PedidoCadastro;
import com.ufes.delivery.util.MoedaUtil;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Tela de resultado do pagamento simulado (US11). Somente leitura.
 * Nenhum campo editavel — todas as informacoes vem do pedido e do resultado simulado.
 */
public class TelaPagamento extends JDialog {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color VERDE  = new Color(0, 128, 0);
    private static final Color VERMELHO = new Color(180, 0, 0);
    private static final Color FUNDO_APROVADO  = new Color(220, 255, 220);
    private static final Color FUNDO_REPROVADO = new Color(255, 220, 220);

    private JButton btnFechar;

    public TelaPagamento(Frame owner, PedidoCadastro pedido, ResultadoPagamento resultado) {
        super(owner, "Pagamento", true);
        construirUI(pedido, resultado);
        pack();
        setMinimumSize(new Dimension(520, 400));
        setLocationRelativeTo(owner);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void construirUI(PedidoCadastro pedido, ResultadoPagamento resultado) {
        boolean aprovado = resultado.isAprovado();

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // --- Cabecalho com resultado ---
        JPanel cabecalho = new JPanel(new GridLayout(aprovado ? 2 : 1, 1, 4, 4));
        cabecalho.setOpaque(true);
        cabecalho.setBackground(aprovado ? FUNDO_APROVADO : FUNDO_REPROVADO);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel lblResultado = new JLabel(
            aprovado ? "Pagamento aprovado" : "Pagamento reprovado",
            SwingConstants.CENTER);
        lblResultado.setFont(lblResultado.getFont().deriveFont(Font.BOLD, 18f));
        lblResultado.setForeground(aprovado ? VERDE : VERMELHO);
        cabecalho.add(lblResultado);

        if (aprovado) {
            JLabel lblPronto = new JLabel("Pedido pronto para entrega", SwingConstants.CENTER);
            lblPronto.setFont(lblPronto.getFont().deriveFont(Font.BOLD, 14f));
            lblPronto.setForeground(VERDE);
            cabecalho.add(lblPronto);
        }

        // --- Corpo com detalhes ---
        JPanel corpo = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 8, 4, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        int linha = 0;

        // Resumo do Pedido
        linha = addSecao(corpo, g, linha, "Resumo do Pedido");
        linha = addLinha(corpo, g, linha, "Pedido:", String.valueOf(pedido.getCodigo()), false);
        linha = addLinha(corpo, g, linha, "Cliente:", pedido.getCliente().getNome(), false);
        linha = addLinha(corpo, g, linha, "Endereco de entrega:", pedido.getEndereco().toString(), false);
        linha = addLinha(corpo, g, linha, "Total do pedido:",
            MoedaUtil.formatar(pedido.getValorTotal()), true);

        if (aprovado) {
            // Informacoes do Pagamento
            linha = addSecao(corpo, g, linha, "Informacoes do Pagamento");
            linha = addLinha(corpo, g, linha, "Situacao do pagamento:", "Aprovado", false);
            linha = addLinha(corpo, g, linha, "Forma de pagamento:",
                resultado.getFormaPagamento(), false);
            linha = addLinha(corpo, g, linha, "Data e hora do pagamento:",
                resultado.getDataHoraTentativa().format(FMT), false);
            linha = addLinha(corpo, g, linha, "Identificador da transacao:",
                resultado.getIdentificadorTransacao(), false);
            linha = addLinha(corpo, g, linha, "Valor pago:",
                MoedaUtil.formatar(resultado.getValorPago()), true);

            // Entrega
            linha = addSecao(corpo, g, linha, "Entrega");
            linha = addLinha(corpo, g, linha, "Situacao do pedido:", "Pronto para entrega", false);
            linha = addLinha(corpo, g, linha, "Prazo estimado de entrega:",
                resultado.getPrazoEstimadoEntrega().format(FMT), true);
            addLinha(corpo, g, linha, "Observacao:",
                "Prazo gerado de forma simulada para o MVP", false);
        } else {
            linha = addSecao(corpo, g, linha, "Informacoes do Pagamento");
            addLinha(corpo, g, linha, "Situacao:", "Reprovado — tente novamente", false);
        }

        // --- Rodape ---
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnFechar = new JButton("Fechar");
        rodape.add(btnFechar);

        root.add(cabecalho, BorderLayout.NORTH);
        root.add(new JScrollPane(corpo), BorderLayout.CENTER);
        root.add(rodape, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private int addSecao(JPanel p, GridBagConstraints g, int linha, String titulo) {
        g.gridx = 0; g.gridy = linha; g.gridwidth = 2; g.weightx = 1;
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 12f));
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        p.add(lbl, g);
        g.gridwidth = 1;
        return linha + 1;
    }

    private int addLinha(JPanel p, GridBagConstraints g, int linha,
                          String rotulo, String valor, boolean destaque) {
        g.gridx = 0; g.gridy = linha; g.weightx = 0;
        p.add(new JLabel(rotulo), g);

        g.gridx = 1; g.weightx = 1;
        JLabel lblValor = new JLabel(valor != null ? valor : "-");
        if (destaque) {
            lblValor.setFont(lblValor.getFont().deriveFont(Font.BOLD, 13f));
            lblValor.setForeground(new Color(0, 80, 160));
        }
        p.add(lblValor, g);
        return linha + 1;
    }

    public JButton getBtnFechar() { return btnFechar; }
}
