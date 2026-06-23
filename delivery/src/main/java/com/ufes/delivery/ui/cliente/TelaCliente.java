package com.ufes.delivery.ui.cliente;

import com.ufes.delivery.model.cadastro.Cliente;
import com.ufes.delivery.model.cadastro.Endereco;
import com.ufes.delivery.model.cadastro.Uf;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Cadastro / visualizacao / edicao de cliente com ate tres enderecos (US06).
 * View passiva: a coluna Padrao e exclusiva (marcar uma desmarca as demais).
 */
public class TelaCliente extends JDialog {

    /** Ordem das colunas da tabela de enderecos. */
    public static final int COL_PADRAO = 0, COL_LOGRADOURO = 1, COL_NUMERO = 2, COL_COMPLEMENTO = 3,
            COL_BAIRRO = 4, COL_CIDADE = 5, COL_UF = 6, COL_CEP = 7;

    private JTextField campoNome;
    private JTextField campoCpf;
    private JTable tabelaEnderecos;
    private DefaultTableModel modeloEnderecos;
    private JButton btnAdicionarEndereco;
    private JButton btnRemoverEndereco;
    private JLabel lblErro;
    private JButton btnSalvar;
    private JButton btnEditar;
    private JButton btnFechar;

    private boolean ajustandoPadrao = false;

    public TelaCliente(Frame owner) {
        super(owner, "Cliente", true);
        construirUI();
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(760, 420));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void construirUI() {
        JPanel topo = new JPanel(new GridBagLayout());
        topo.setBorder(BorderFactory.createTitledBorder("Dados do Cliente"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        topo.add(new JLabel("Nome:"), gbc);
        campoNome = new JTextField(28);
        gbc.gridx = 1; gbc.weightx = 1;
        topo.add(campoNome, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        topo.add(new JLabel("CPF:"), gbc);
        campoCpf = new JTextField(28);
        gbc.gridx = 1; gbc.weightx = 1;
        topo.add(campoCpf, gbc);

        String[] colunas = {"Padrao", "Logradouro", "Numero", "Complemento", "Bairro", "Cidade", "UF", "CEP"};
        modeloEnderecos = new DefaultTableModel(colunas, 0) {
            @Override public Class<?> getColumnClass(int c) {
                return c == COL_PADRAO ? Boolean.class : String.class;
            }
            @Override public boolean isCellEditable(int r, int c) {
                return btnSalvar.isVisible(); // editavel apenas em modo edicao
            }
        };
        modeloEnderecos.addTableModelListener(e -> {
            if (ajustandoPadrao) return;
            if (e.getColumn() == COL_PADRAO) garantirPadraoUnico(e.getFirstRow());
        });

        tabelaEnderecos = new JTable(modeloEnderecos);
        tabelaEnderecos.setRowHeight(24);
        tabelaEnderecos.getColumnModel().getColumn(COL_PADRAO).setMaxWidth(60);
        JComboBox<Uf> comboUf = new JComboBox<>(Uf.values());
        tabelaEnderecos.getColumnModel().getColumn(COL_UF)
            .setCellEditor(new DefaultCellEditor(comboUf));

        JScrollPane scroll = new JScrollPane(tabelaEnderecos);
        scroll.setBorder(BorderFactory.createTitledBorder("Enderecos de entrega (1 a 3, um padrao)"));

        JPanel painelEnderecoBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        btnAdicionarEndereco = new JButton("Adicionar endereco");
        btnRemoverEndereco = new JButton("Remover endereco");
        painelEnderecoBotoes.add(btnAdicionarEndereco);
        painelEnderecoBotoes.add(btnRemoverEndereco);

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(scroll, BorderLayout.CENTER);
        centro.add(painelEnderecoBotoes, BorderLayout.SOUTH);

        lblErro = new JLabel(" ");
        lblErro.setForeground(Color.RED);
        lblErro.setFont(lblErro.getFont().deriveFont(Font.PLAIN, 11f));

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnSalvar = new JButton("Salvar");
        btnEditar = new JButton("Editar");
        btnFechar = new JButton("Fechar");
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnEditar);
        painelBotoes.add(btnFechar);

        JPanel rodape = new JPanel(new BorderLayout());
        rodape.add(lblErro, BorderLayout.WEST);
        rodape.add(painelBotoes, BorderLayout.EAST);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.add(topo, BorderLayout.NORTH);
        root.add(centro, BorderLayout.CENTER);
        root.add(rodape, BorderLayout.SOUTH);
        setContentPane(root);
    }

    /** Garante exclusividade do endereco padrao. */
    private void garantirPadraoUnico(int linhaMarcada) {
        Object valor = modeloEnderecos.getValueAt(linhaMarcada, COL_PADRAO);
        if (!Boolean.TRUE.equals(valor)) return;
        ajustandoPadrao = true;
        try {
            for (int i = 0; i < modeloEnderecos.getRowCount(); i++) {
                if (i != linhaMarcada) modeloEnderecos.setValueAt(false, i, COL_PADRAO);
            }
        } finally {
            ajustandoPadrao = false;
        }
    }

    public void adicionarLinhaEndereco() {
        if (modeloEnderecos.getRowCount() >= Cliente.MAX_ENDERECOS) {
            setMensagemErro("Limite de " + Cliente.MAX_ENDERECOS + " enderecos atingido");
            return;
        }
        boolean primeiro = modeloEnderecos.getRowCount() == 0;
        modeloEnderecos.addRow(new Object[]{primeiro, "", "", "", "", "", Uf.ES.name(), ""});
    }

    public void removerLinhaEnderecoSelecionada() {
        int linha = tabelaEnderecos.getSelectedRow();
        if (linha < 0) {
            setMensagemErro("Selecione um endereco para remover");
            return;
        }
        if (tabelaEnderecos.isEditing()) tabelaEnderecos.getCellEditor().stopCellEditing();
        modeloEnderecos.removeRow(linha);
    }

    /** Preenche a tela com cliente persistido e entra em modo leitura. */
    public void preencher(Cliente cliente) {
        campoNome.setText(cliente.getNome());
        campoCpf.setText(cliente.getCpfFormatado());
        modeloEnderecos.setRowCount(0);
        for (Endereco e : cliente.getEnderecos()) {
            modeloEnderecos.addRow(new Object[]{
                e.isPadrao(), e.getLogradouro(), e.getNumero(),
                e.getComplemento() == null ? "" : e.getComplemento(),
                e.getBairro(), e.getCidade(), e.getUf().name(), e.getCepFormatado()
            });
        }
        definirModoLeitura(true);
        setTitle("Cliente - " + cliente.getNome());
    }

    public void definirModoLeitura(boolean leitura) {
        boolean editavel = !leitura;
        campoNome.setEnabled(editavel);
        campoCpf.setEnabled(editavel);
        btnAdicionarEndereco.setEnabled(editavel);
        btnRemoverEndereco.setEnabled(editavel);
        btnSalvar.setVisible(editavel);
        btnEditar.setVisible(leitura);
    }

    public void definirModoNovo() {
        definirModoLeitura(false);
        btnEditar.setVisible(false);
    }

    public String getNome() { return campoNome.getText().trim(); }
    public String getCpf() { return campoCpf.getText().trim(); }

    /** Retorna as linhas de endereco como objetos brutos na ordem das colunas. */
    public List<Object[]> getLinhasEndereco() {
        if (tabelaEnderecos.isEditing()) tabelaEnderecos.getCellEditor().stopCellEditing();
        List<Object[]> linhas = new ArrayList<>();
        for (int i = 0; i < modeloEnderecos.getRowCount(); i++) {
            Object[] linha = new Object[8];
            for (int c = 0; c < 8; c++) linha[c] = modeloEnderecos.getValueAt(i, c);
            linhas.add(linha);
        }
        return linhas;
    }

    public void setMensagemErro(String msg) {
        lblErro.setForeground(Color.RED);
        lblErro.setText(msg == null ? " " : msg);
    }
    public void setMensagemSucesso(String msg) {
        lblErro.setForeground(new Color(0, 128, 0));
        lblErro.setText(msg);
    }

    public JButton getBtnAdicionarEndereco() { return btnAdicionarEndereco; }
    public JButton getBtnRemoverEndereco() { return btnRemoverEndereco; }
    public JButton getBtnSalvar() { return btnSalvar; }
    public JButton getBtnEditar() { return btnEditar; }
    public JButton getBtnFechar() { return btnFechar; }
}
