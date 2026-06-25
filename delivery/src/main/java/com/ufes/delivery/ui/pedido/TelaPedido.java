package com.ufes.delivery.ui.pedido;

import com.ufes.delivery.model.cadastro.Cliente;
import com.ufes.delivery.model.cadastro.Endereco;
import com.ufes.delivery.model.cadastro.Produto;
import com.ufes.delivery.model.pedido.PedidoItem;
import com.ufes.delivery.util.MoedaUtil;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

public class TelaPedido extends JDialog {

    private JComboBox<Cliente> comboClientes;
    private JComboBox<Endereco> comboEnderecos;
    private JComboBox<Produto> comboProdutos;
    private JSpinner spinnerQuantidade;
    private JTextField campoCupom;
    private JTable tabelaItens;
    private DefaultTableModel modeloItens;
    private JLabel lblSubtotal;
    private JLabel lblDesconto;
    private JLabel lblTaxaEntrega;
    private JLabel lblTotal;
    private JLabel lblMensagem;
    private JButton btnAdicionarItem;
    private JButton btnSalvar;
    private JButton btnCancelar;
    private JButton btnNovoCliente;
    private JMenuItem menuExcluirItem;
    private final List<PedidoItem> itens = new ArrayList<>();

    public TelaPedido(Frame owner) {
        super(owner, "Novo Pedido", true);
        construirUI();
        setSize(820, 560);
        setMinimumSize(new Dimension(760, 520));
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void construirUI() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.add(construirTopo(), BorderLayout.NORTH);
        root.add(construirTabela(), BorderLayout.CENTER);
        root.add(construirRodape(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel construirTopo() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Dados do pedido"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        comboClientes = new JComboBox<>();
        comboEnderecos = new JComboBox<>();
        comboProdutos = new JComboBox<>();
        configurarRenderers();
        spinnerQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        btnAdicionarItem = new JButton("Adicionar item");
        btnNovoCliente = new JButton("Novo cliente");
        campoCupom = new JTextField(12);

        // Row 0
        g.gridx = 0; g.gridy = 0; g.weightx = 0; g.gridwidth = 1;
        painel.add(new JLabel("Cliente:"), g);
        g.gridx = 1; g.weightx = 1; g.gridwidth = 3;
        painel.add(comboClientes, g);
        g.gridx = 4; g.weightx = 0; g.gridwidth = 1;
        painel.add(btnNovoCliente, g);

        // Row 1
        g.gridx = 0; g.gridy = 1; g.weightx = 0; g.gridwidth = 1;
        painel.add(new JLabel("Endereco:"), g);
        g.gridx = 1; g.weightx = 1; g.gridwidth = 4;
        painel.add(comboEnderecos, g);

        // Row 2
        g.gridx = 0; g.gridy = 2; g.weightx = 0; g.gridwidth = 1;
        painel.add(new JLabel("Produto:"), g);
        g.gridx = 1; g.weightx = 1; g.gridwidth = 1;
        painel.add(comboProdutos, g);

        g.gridx = 2; g.gridy = 2; g.weightx = 0; g.gridwidth = 1;
        painel.add(new JLabel("Qtd:"), g);
        g.gridx = 3; g.weightx = 0; g.gridwidth = 1;
        painel.add(spinnerQuantidade, g);
        g.gridx = 4; g.weightx = 0; g.gridwidth = 1;
        painel.add(btnAdicionarItem, g);

        // Row 3
        g.gridx = 0; g.gridy = 3; g.weightx = 0; g.gridwidth = 1;
        painel.add(new JLabel("Cupom:"), g);
        g.gridx = 1; g.weightx = 1; g.gridwidth = 4;
        painel.add(campoCupom, g);

        // Reset gridwidth for any other components
        g.gridwidth = 1;

        return painel;
    }

    private void configurarRenderers() {
        comboClientes.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Cliente c) {
                    setText(c.getNome() + " - " + c.getCpfFormatado());
                }
                return this;
            }
        });
        comboEnderecos.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Endereco e) {
                    setText(e.toString());
                }
                return this;
            }
        });
        comboProdutos.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Produto p) {
                    setText(p.getCodigo() + " - " + p.getNome()
                        + " (" + MoedaUtil.formatar(p.getPrecoUnitario()) + ")");
                }
                return this;
            }
        });
    }

    private JScrollPane construirTabela() {
        String[] colunas = {"Codigo", "Produto", "Categoria", "Qtd", "Unitario", "Subtotal"};
        modeloItens = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 3; }
            @Override public Class<?> getColumnClass(int col) {
                return col == 3 ? Integer.class : String.class;
            }
        };
        tabelaItens = new JTable(modeloItens);
        tabelaItens.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaItens.setRowHeight(24);

        JPopupMenu popup = new JPopupMenu();
        menuExcluirItem = new JMenuItem("Excluir item");
        popup.add(menuExcluirItem);
        tabelaItens.setComponentPopupMenu(popup);

        JScrollPane scroll = new JScrollPane(tabelaItens);
        scroll.setBorder(BorderFactory.createTitledBorder("Itens do pedido"));
        return scroll;
    }

    private JPanel construirRodape() {
        JPanel rodape = new JPanel(new BorderLayout());
        JPanel totais = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        lblSubtotal = new JLabel("Subtotal: R$ 0,00");
        lblDesconto = new JLabel("Desconto: R$ 0,00");
        lblTaxaEntrega = new JLabel("Taxa de entrega: R$ 0,00");
        lblTotal = new JLabel("Total: R$ 0,00");
        totais.add(lblSubtotal);
        totais.add(lblDesconto);
        totais.add(lblTaxaEntrega);
        totais.add(lblTotal);

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        lblMensagem = new JLabel(" ");
        btnSalvar = new JButton("Pagar");
        btnCancelar = new JButton("Cancelar");
        acoes.add(lblMensagem);
        acoes.add(btnSalvar);
        acoes.add(btnCancelar);

        rodape.add(totais, BorderLayout.NORTH);
        rodape.add(acoes, BorderLayout.SOUTH);
        return rodape;
    }

    public void carregarClientes(List<Cliente> clientes) {
        comboClientes.removeAllItems();
        clientes.forEach(comboClientes::addItem);
    }

    public void carregarEnderecos(List<Endereco> enderecos) {
        comboEnderecos.removeAllItems();
        enderecos.forEach(comboEnderecos::addItem);
    }

    public void carregarProdutos(List<Produto> produtos) {
        comboProdutos.removeAllItems();
        produtos.forEach(comboProdutos::addItem);
    }

    public Cliente getClienteSelecionado() { return (Cliente) comboClientes.getSelectedItem(); }
    public Endereco getEnderecoSelecionado() { return (Endereco) comboEnderecos.getSelectedItem(); }
    public Produto getProdutoSelecionado() { return (Produto) comboProdutos.getSelectedItem(); }
    public int getQuantidade() { return (Integer) spinnerQuantidade.getValue(); }
    public String getCupom() { return campoCupom.getText().trim(); }
    public JTable getTabelaItens() { return tabelaItens; }
    public DefaultTableModel getModeloItens() { return modeloItens; }
    public JButton getBtnAdicionarItem() { return btnAdicionarItem; }
    public JButton getBtnPagar() { return btnSalvar; }
    public JButton getBtnCancelar() { return btnCancelar; }
    public JButton getBtnNovoCliente() { return btnNovoCliente; }
    public JComboBox<Cliente> getComboClientes() { return comboClientes; }
    public JTextField getCampoCupom() { return campoCupom; }
    public JMenuItem getMenuExcluirItem() { return menuExcluirItem; }

    public void selecionarEndereco(Endereco endereco) {
        comboEnderecos.setSelectedItem(endereco);
    }

    public List<PedidoItem> getItens() {
        return new ArrayList<>(itens);
    }

    public void adicionarItem(PedidoItem item) {
        itens.add(item);
        recarregarItens();
    }

    public void removerItemSelecionado() {
        int linha = tabelaItens.getSelectedRow();
        if (linha >= 0) {
            itens.remove(linha);
            recarregarItens();
        }
    }

    public void atualizarQuantidade(int linha, int quantidade) {
        if (linha >= 0 && linha < itens.size()) {
            itens.get(linha).setQuantidade(quantidade);
            recarregarItens();
        }
    }

    public void setTotais(BigDecimal subtotal, BigDecimal desconto,
                          BigDecimal taxaEntregaFinal, BigDecimal total) {
        lblSubtotal.setText("Subtotal: " + MoedaUtil.formatar(subtotal));
        lblDesconto.setText("Desconto: " + MoedaUtil.formatar(desconto));
        lblTaxaEntrega.setText("Taxa de entrega: " + MoedaUtil.formatar(taxaEntregaFinal));
        lblTotal.setText("Total: " + MoedaUtil.formatar(total));
    }

    public void setMensagem(String msg) {
        lblMensagem.setText(msg == null || msg.isBlank() ? " " : msg);
    }

    private void recarregarItens() {
        int linhaSelecionada = tabelaItens.getSelectedRow();
        modeloItens.setRowCount(0);
        for (PedidoItem item : itens) {
            modeloItens.addRow(new Object[]{
                String.valueOf(item.getProdutoCodigo()),
                item.getProdutoNome(),
                item.getCategoria().getDescricao(),
                item.getQuantidade(),
                MoedaUtil.formatar(item.getPrecoUnitario()),
                MoedaUtil.formatar(item.getSubtotal())
            });
        }
        if (linhaSelecionada >= 0 && linhaSelecionada < modeloItens.getRowCount()) {
            tabelaItens.setRowSelectionInterval(linhaSelecionada, linhaSelecionada);
        }
    }
}
