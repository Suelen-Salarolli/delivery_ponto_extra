package com.ufes.delivery.ui.cliente;

import com.ufes.delivery.model.cadastro.Cliente;
import com.ufes.delivery.service.ClienteService.AtributoBusca;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Busca de clientes por Nome ou CPF (US05). View passiva.
 */
public class TelaBuscaClientes extends JFrame {

    private JComboBox<String> comboAtributo;
    private JTextField campoValor;
    private JButton btnBuscar;
    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private JButton btnNovo;
    private JButton btnVisualizar;
    private JButton btnFechar;
    private JLabel lblStatus;

    private List<Cliente> clientesCarregados = new ArrayList<>();

    public TelaBuscaClientes() {
        super("Busca de Clientes");
        construirUI();
        setSize(720, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void construirUI() {
        setLayout(new BorderLayout(8, 8));

        JPanel painelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        painelBusca.setBorder(BorderFactory.createTitledBorder("Busca de Clientes"));
        painelBusca.add(new JLabel("Atributo:"));
        comboAtributo = new JComboBox<>(new String[]{"Nome", "CPF"});
        painelBusca.add(comboAtributo);
        painelBusca.add(new JLabel("Valor:"));
        campoValor = new JTextField(24);
        painelBusca.add(campoValor);
        btnBuscar = new JButton("Buscar");
        painelBusca.add(btnBuscar);

        String[] colunas = {"Nome", "CPF"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabela = new JTable(modeloTabela);
        tabela.setRowHeight(24);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createTitledBorder("Clientes"));

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

    public void carregarClientes(List<Cliente> clientes) {
        modeloTabela.setRowCount(0);
        clientesCarregados = new ArrayList<>(clientes);
        for (Cliente c : clientes) {
            modeloTabela.addRow(new Object[]{c.getNome(), c.getCpfFormatado()});
        }
    }

    public AtributoBusca getAtributoBusca() {
        return "CPF".equals(comboAtributo.getSelectedItem()) ? AtributoBusca.CPF : AtributoBusca.NOME;
    }

    public String getValorBusca() { return campoValor.getText().trim(); }

    public Cliente getClienteSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) return null;
        return clientesCarregados.get(linha);
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
