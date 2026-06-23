package com.ufes.delivery.ui.produto;

import com.ufes.delivery.model.cadastro.Categoria;
import com.ufes.delivery.model.cadastro.Produto;
import com.ufes.delivery.service.ProdutoService.AtributoBusca;
import com.ufes.delivery.util.MoedaUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Busca de produtos (US07). View passiva.
 */
public class TelaBuscaProdutos extends JFrame {

    private JComboBox<String> comboAtributo;
    private JTextField campoValorTexto;
    private JComboBox<Categoria> comboValorCategoria;
    private JPanel painelValor;
    private JButton btnBuscar;
    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private JButton btnNovo;
    private JButton btnVisualizar;
    private JButton btnFechar;
    private JLabel lblStatus;

    private List<Produto> produtosCarregados = new ArrayList<>();

    private static final String CARD_TEXTO = "texto";
    private static final String CARD_CATEGORIA = "categoria";

    public TelaBuscaProdutos() {
        super("Busca de Produtos");
        construirUI();
        setSize(820, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void construirUI() {
        setLayout(new BorderLayout(8, 8));

        JPanel painelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        painelBusca.setBorder(BorderFactory.createTitledBorder("Busca de Produtos"));
        painelBusca.add(new JLabel("Atributo:"));
        comboAtributo = new JComboBox<>(new String[]{"Codigo", "Nome", "Categoria"});
        painelBusca.add(comboAtributo);

        painelBusca.add(new JLabel("Valor:"));
        painelValor = new JPanel(new CardLayout());
        campoValorTexto = new JTextField(20);
        comboValorCategoria = new JComboBox<>(Categoria.values());
        painelValor.add(campoValorTexto, CARD_TEXTO);
        painelValor.add(comboValorCategoria, CARD_CATEGORIA);
        painelBusca.add(painelValor);

        btnBuscar = new JButton("Buscar");
        painelBusca.add(btnBuscar);

        comboAtributo.addActionListener(e -> atualizarCampoValor());

        String[] colunas = {"Codigo", "Nome", "Categoria", "Preco unitario", "Estoque atual"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabela = new JTable(modeloTabela);
        tabela.setRowHeight(24);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createTitledBorder("Produtos"));

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnNovo = new JButton("Novo");
        btnVisualizar = new JButton("Visualizar");
        btnFechar = new JButton("Fechar");
        painelBotoes.add(btnNovo);
        painelBotoes.add(btnVisualizar);
        painelBotoes.add(btnFechar);

        lblStatus = new JLabel(" ");
        lblStatus.setFont(lblStatus.getFont().deriveFont(Font.PLAIN, 11f));
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.add(lblStatus, BorderLayout.WEST);
        rodape.add(painelBotoes, BorderLayout.EAST);

        add(painelBusca, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(rodape, BorderLayout.SOUTH);
    }

    private void atualizarCampoValor() {
        CardLayout cl = (CardLayout) painelValor.getLayout();
        cl.show(painelValor, "Categoria".equals(getAtributoSelecionado()) ? CARD_CATEGORIA : CARD_TEXTO);
    }

    public void carregarProdutos(List<Produto> produtos) {
        modeloTabela.setRowCount(0);
        produtosCarregados = new ArrayList<>(produtos);
        for (Produto p : produtos) {
            modeloTabela.addRow(new Object[]{
                p.getCodigo(),
                p.getNome(),
                p.getCategoria().getDescricao(),
                MoedaUtil.formatar(p.getPrecoUnitario()),
                p.getEstoqueAtual()
            });
        }
    }

    public AtributoBusca getAtributoBusca() {
        return switch (getAtributoSelecionado()) {
            case "Codigo" -> AtributoBusca.CODIGO;
            case "Categoria" -> AtributoBusca.CATEGORIA;
            default -> AtributoBusca.NOME;
        };
    }

    private String getAtributoSelecionado() { return (String) comboAtributo.getSelectedItem(); }

    public String getValorBusca() {
        if ("Categoria".equals(getAtributoSelecionado())) {
            return ((Categoria) comboValorCategoria.getSelectedItem()).getDescricao();
        }
        return campoValorTexto.getText().trim();
    }

    public Produto getProdutoSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) return null;
        return produtosCarregados.get(linha);
    }

    public void setMensagem(String msg, boolean erro) {
        lblStatus.setForeground(erro ? Color.RED : new Color(0, 120, 0));
        lblStatus.setText(msg);
    }

    public JButton getBtnBuscar() { return btnBuscar; }
    public JButton getBtnNovo() { return btnNovo; }
    public JButton getBtnVisualizar() { return btnVisualizar; }
    public JButton getBtnFechar() { return btnFechar; }
    public JTable getTabela() { return tabela; }
}
