package com.ufes.delivery.ui.estoque;

import com.ufes.delivery.model.cadastro.MovimentacaoEstoque;
import com.ufes.delivery.model.cadastro.Produto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Tela de movimentacao de estoque (US08). View passiva.
 *
 * Layout:
 *  - Busca de produto (campo texto + botao Buscar + tabela de resultados + botao Selecionar)
 *  - Produto Selecionado (campos leitura: nome, estoque atual)
 *  - Movimentacao (data, tipo, quantidade, motivo/NF, previa)
 *  - Rodape: aviso informativo + Confirmar + Cancelar
 */
public class TelaMovimentacaoEstoque extends JFrame {

    // --- Busca ---
    private JTextField campoBuscaProduto;
    private JButton btnBuscar;
    private JTable tabelaProdutos;
    private DefaultTableModel modeloTabelaProdutos;
    private JButton btnSelecionar;

    // --- Produto selecionado ---
    private JLabel lblProdutoNome;
    private JLabel lblEstoqueAtual;

    // --- Movimentacao ---
    private JFormattedTextField campoData;
    private JComboBox<String> comboTipo;
    private JSpinner spinnerQuantidade;
    private JTextField campoMotivo;
    private JTextField campoNotaFiscal;
    private JLabel lblPrevia;
    private JLabel lblAvisoPrevia;

    // --- Rodape ---
    private JLabel lblErro;
    private JButton btnConfirmar;
    private JButton btnCancelar;

    private List<Produto> produtosCarregados = new ArrayList<>();

    public TelaMovimentacaoEstoque(Frame owner) {
        super("Movimentacao de Estoque");
        construirUI();
        setSize(780, 660);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void construirUI() {
        setLayout(new BorderLayout(8, 8));

        // ---- BUSCA ----
        JPanel painelBusca = new JPanel(new BorderLayout(4, 4));
        painelBusca.setBorder(BorderFactory.createTitledBorder("Busca de Produtos"));

        JPanel linhaBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        linhaBusca.add(new JLabel("Buscar produto:"));
        campoBuscaProduto = new JTextField(30);
        linhaBusca.add(campoBuscaProduto);
        btnBuscar = new JButton("Buscar");
        linhaBusca.add(btnBuscar);
        painelBusca.add(linhaBusca, BorderLayout.NORTH);

        String[] colsProduto = {"Codigo", "Produto", "Categoria", "Estoque atual"};
        modeloTabelaProdutos = new DefaultTableModel(colsProduto, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaProdutos = new JTable(modeloTabelaProdutos);
        tabelaProdutos.setRowHeight(22);
        tabelaProdutos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaProdutos.getColumnModel().getColumn(0).setMaxWidth(70);
        tabelaProdutos.getColumnModel().getColumn(3).setMaxWidth(100);
        JScrollPane scrollProdutos = new JScrollPane(tabelaProdutos);
        scrollProdutos.setPreferredSize(new Dimension(700, 110));
        painelBusca.add(scrollProdutos, BorderLayout.CENTER);

        JPanel linhaSelecionar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btnSelecionar = new JButton("Selecionar");
        linhaSelecionar.add(btnSelecionar);
        painelBusca.add(linhaSelecionar, BorderLayout.SOUTH);

        // ---- PRODUTO SELECIONADO ----
        JPanel painelSelecionado = new JPanel(new GridBagLayout());
        painelSelecionado.setBorder(BorderFactory.createTitledBorder("Produto Selecionado"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 8, 4, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        painelSelecionado.add(new JLabel("Produto:"), g);
        lblProdutoNome = new JLabel("(nenhum selecionado)");
        lblProdutoNome.setFont(lblProdutoNome.getFont().deriveFont(Font.BOLD));
        g.gridx = 1; g.weightx = 1;
        painelSelecionado.add(lblProdutoNome, g);

        g.gridx = 0; g.gridy = 1; g.weightx = 0;
        painelSelecionado.add(new JLabel("Quantidade atual em estoque:"), g);
        lblEstoqueAtual = new JLabel("-");
        lblEstoqueAtual.setFont(lblEstoqueAtual.getFont().deriveFont(Font.BOLD));
        g.gridx = 1; g.weightx = 1;
        painelSelecionado.add(lblEstoqueAtual, g);

        // ---- MOVIMENTACAO ----
        JPanel painelMov = new JPanel(new GridBagLayout());
        painelMov.setBorder(BorderFactory.createTitledBorder("Movimentacao"));
        GridBagConstraints gm = new GridBagConstraints();
        gm.insets = new Insets(4, 8, 4, 8);
        gm.fill = GridBagConstraints.HORIZONTAL;

        // Linha 0: data | tipo
        gm.gridx = 0; gm.gridy = 0; gm.weightx = 0;
        painelMov.add(new JLabel("Data da movimentacao:"), gm);
        campoData = new JFormattedTextField(
            new java.text.SimpleDateFormat("dd/MM/yyyy"));
        campoData.setColumns(10);
        campoData.setValue(new java.util.Date());
        gm.gridx = 1; gm.weightx = 0.3;
        painelMov.add(campoData, gm);

        gm.gridx = 2; gm.weightx = 0;
        painelMov.add(new JLabel("Tipo de movimentacao:"), gm);
        comboTipo = new JComboBox<>(new String[]{
            MovimentacaoEstoque.TIPO_ENTRADA,
            MovimentacaoEstoque.TIPO_AJUSTE
        });
        gm.gridx = 3; gm.weightx = 0.7;
        painelMov.add(comboTipo, gm);

        // Linha 1: quantidade | motivo
        gm.gridx = 0; gm.gridy = 1; gm.weightx = 0;
        painelMov.add(new JLabel("Quantidade a movimentar:"), gm);
        spinnerQuantidade = new JSpinner(new SpinnerNumberModel(1, -9999, 9999, 1));
        gm.gridx = 1; gm.weightx = 0.3;
        painelMov.add(spinnerQuantidade, gm);

        gm.gridx = 2; gm.weightx = 0;
        painelMov.add(new JLabel("Motivo do ajuste:"), gm);
        campoMotivo = new JTextField(20);
        gm.gridx = 3; gm.weightx = 0.7;
        painelMov.add(campoMotivo, gm);

        // Linha 2: previa | nota fiscal
        gm.gridx = 0; gm.gridy = 2; gm.weightx = 0;
        painelMov.add(new JLabel("Estoque apos movimentacao (previa):"), gm);
        lblPrevia = new JLabel("-");
        lblPrevia.setFont(lblPrevia.getFont().deriveFont(Font.BOLD, 14f));
        gm.gridx = 1; gm.weightx = 0.3;
        painelMov.add(lblPrevia, gm);

        gm.gridx = 2; gm.weightx = 0;
        painelMov.add(new JLabel("Nota fiscal de entrada:"), gm);
        campoNotaFiscal = new JTextField(20);
        gm.gridx = 3; gm.weightx = 0.7;
        painelMov.add(campoNotaFiscal, gm);

        // Linha 3: aviso
        lblAvisoPrevia = new JLabel(
            "Pre-visualizacao. Ajustes requerem motivo. Entradas requerem nota fiscal.");
        lblAvisoPrevia.setFont(lblAvisoPrevia.getFont().deriveFont(Font.ITALIC, 11f));
        lblAvisoPrevia.setForeground(new Color(80, 80, 180));
        gm.gridx = 0; gm.gridy = 3; gm.gridwidth = 4; gm.weightx = 1;
        painelMov.add(lblAvisoPrevia, gm);

        // ---- CENTRO: selecionado + movimentacao ----
        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.add(painelSelecionado);
        centro.add(Box.createVerticalStrut(4));
        centro.add(painelMov);

        // ---- RODAPE ----
        lblErro = new JLabel(" ");
        lblErro.setForeground(Color.RED);
        lblErro.setFont(lblErro.getFont().deriveFont(Font.PLAIN, 11f));

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnConfirmar = new JButton("Confirmar movimentacao");
        btnCancelar = new JButton("Cancelar");
        painelBotoes.add(btnConfirmar);
        painelBotoes.add(btnCancelar);

        JPanel rodape = new JPanel(new BorderLayout());
        rodape.add(lblErro, BorderLayout.WEST);
        rodape.add(painelBotoes, BorderLayout.EAST);

        add(painelBusca, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(rodape, BorderLayout.SOUTH);
    }

    // --- Populacao da tabela de resultados ---
    public void carregarProdutos(List<Produto> produtos) {
        modeloTabelaProdutos.setRowCount(0);
        produtosCarregados = new ArrayList<>(produtos);
        for (Produto p : produtos) {
            modeloTabelaProdutos.addRow(new Object[]{
                p.getCodigo(), p.getNome(),
                p.getCategoria().getDescricao(), p.getEstoqueAtual()
            });
        }
    }

    public Produto getProdutoDaBuscaSelecionado() {
        int linha = tabelaProdutos.getSelectedRow();
        return linha < 0 ? null : produtosCarregados.get(linha);
    }

    // --- Exibir produto selecionado ---
    public void exibirProdutoSelecionado(Produto p) {
        if (p == null) {
            lblProdutoNome.setText("(nenhum selecionado)");
            lblEstoqueAtual.setText("-");
            lblPrevia.setText("-");
        } else {
            lblProdutoNome.setText("[" + p.getCodigo() + "] " + p.getNome());
            lblEstoqueAtual.setText(String.valueOf(p.getEstoqueAtual()));
            lblPrevia.setText(String.valueOf(p.getEstoqueAtual())); // previa inicial = atual
        }
    }

    public void setPrevia(int valor) {
        lblPrevia.setText(String.valueOf(valor));
        lblPrevia.setForeground(valor < 0 ? Color.RED : new Color(0, 100, 0));
    }

    // --- Atualizacao do estado de campos por tipo ---
    public void atualizarCamposPorTipo(String tipo) {
        boolean isAjuste = MovimentacaoEstoque.TIPO_AJUSTE.equals(tipo);
        campoMotivo.setEnabled(isAjuste);
        campoNotaFiscal.setEnabled(!isAjuste);
        if (isAjuste) campoNotaFiscal.setText("");
        else campoMotivo.setText("");
    }

    // --- Getters para o Presenter ---
    public String getTextoBusca() { return campoBuscaProduto.getText().trim(); }
    public String getTipo() { return (String) comboTipo.getSelectedItem(); }
    public int getQuantidade() { return (Integer) spinnerQuantidade.getValue(); }
    public String getMotivo() { return campoMotivo.getText().trim(); }
    public String getNotaFiscal() { return campoNotaFiscal.getText().trim(); }

    public java.time.LocalDate getDataMovimentacao() {
        try {
            // JFormattedTextField retorna java.util.Date
            java.util.Date d = (java.util.Date) campoData.getValue();
            return d.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
        } catch (Exception e) {
            return null;
        }
    }

    public void setMensagemErro(String msg) {
        lblErro.setForeground(Color.RED);
        lblErro.setText(msg == null ? " " : msg);
    }
    public void setMensagemSucesso(String msg) {
        lblErro.setForeground(new Color(0, 128, 0));
        lblErro.setText(msg);
    }

    public JTextField getCampoBuscaProduto() { return campoBuscaProduto; }
    public JButton getBtnBuscar() { return btnBuscar; }
    public JButton getBtnSelecionar() { return btnSelecionar; }
    public JSpinner getSpinnerQuantidade() { return spinnerQuantidade; }
    public JComboBox<String> getComboTipo() { return comboTipo; }
    public JButton getBtnConfirmar() { return btnConfirmar; }
    public JButton getBtnCancelar() { return btnCancelar; }
}
