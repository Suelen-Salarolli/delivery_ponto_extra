package com.ufes.delivery.ui.produto;

import com.ufes.delivery.model.cadastro.Categoria;
import com.ufes.delivery.model.cadastro.Produto;

import javax.swing.*;
import java.awt.*;

/**
 * Cadastro / visualizacao / edicao de produto (US07).
 * View passiva: nao conhece servico; expoe getters/comandos ao presenter.
 */
public class TelaProduto extends JDialog {

    private JTextField campoCodigo;
    private JTextField campoNome;
    private JComboBox<Categoria> comboCategoria;
    private JTextField campoPreco;
    private JTextField campoEstoque;
    private JLabel lblErro;
    private JButton btnSalvar;
    private JButton btnEditar;
    private JButton btnFechar;

    public TelaProduto(Frame owner) {
        super(owner, "Produto", true);
        construirUI();
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void construirUI() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Dados do Produto"));
        painel.setPreferredSize(new Dimension(440, 230));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        painel.add(new JLabel("Codigo:"), gbc);
        campoCodigo = new JTextField(20);
        somenteInteiros(campoCodigo);
        gbc.gridx = 1; gbc.weightx = 1;
        painel.add(campoCodigo, gbc);

        gbc.gridx = 0; gbc.gridy = ++y; gbc.weightx = 0;
        painel.add(new JLabel("Nome:"), gbc);
        campoNome = new JTextField(20);
        gbc.gridx = 1; gbc.weightx = 1;
        painel.add(campoNome, gbc);

        gbc.gridx = 0; gbc.gridy = ++y; gbc.weightx = 0;
        painel.add(new JLabel("Categoria:"), gbc);
        comboCategoria = new JComboBox<>(Categoria.values());
        gbc.gridx = 1; gbc.weightx = 1;
        painel.add(comboCategoria, gbc);

        gbc.gridx = 0; gbc.gridy = ++y; gbc.weightx = 0;
        painel.add(new JLabel("Preco unitario (R$):"), gbc);
        campoPreco = new JTextField(20);
        gbc.gridx = 1; gbc.weightx = 1;
        painel.add(campoPreco, gbc);

        gbc.gridx = 0; gbc.gridy = ++y; gbc.weightx = 0;
        painel.add(new JLabel("Estoque inicial:"), gbc);
        campoEstoque = new JTextField(20);
        somenteInteiros(campoEstoque);
        gbc.gridx = 1; gbc.weightx = 1;
        painel.add(campoEstoque, gbc);

        lblErro = new JLabel(" ");
        lblErro.setForeground(Color.RED);
        lblErro.setFont(lblErro.getFont().deriveFont(Font.PLAIN, 11f));
        gbc.gridx = 0; gbc.gridy = ++y; gbc.gridwidth = 2;
        painel.add(lblErro, gbc);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        btnSalvar = new JButton("Salvar");
        btnEditar = new JButton("Editar");
        btnFechar = new JButton("Fechar");
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnEditar);
        painelBotoes.add(btnFechar);

        getRootPane().setDefaultButton(btnSalvar);

        JPanel root = new JPanel(new BorderLayout());
        root.add(painel, BorderLayout.CENTER);
        root.add(painelBotoes, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void somenteInteiros(JTextField campo) {
        ((javax.swing.text.AbstractDocument) campo.getDocument())
            .setDocumentFilter(new javax.swing.text.DocumentFilter() {
                @Override public void insertString(FilterBypass fb, int off, String s, javax.swing.text.AttributeSet a)
                        throws javax.swing.text.BadLocationException {
                    if (s.matches("\\d*")) super.insertString(fb, off, s, a);
                }
                @Override public void replace(FilterBypass fb, int off, int len, String s, javax.swing.text.AttributeSet a)
                        throws javax.swing.text.BadLocationException {
                    if (s.matches("\\d*")) super.replace(fb, off, len, s, a);
                }
            });
    }

    /** Preenche a tela com um produto existente e entra em modo leitura. */
    public void preencher(Produto p) {
        campoCodigo.setText(String.valueOf(p.getCodigo()));
        campoNome.setText(p.getNome());
        comboCategoria.setSelectedItem(p.getCategoria());
        campoPreco.setText(com.ufes.delivery.util.MoedaUtil.formatarSemSimbolo(p.getPrecoUnitario()));
        campoEstoque.setText(String.valueOf(p.getEstoqueAtual()));
        definirModoLeitura(true);
        setTitle("Produto " + p.getCodigo());
    }

    /** Alterna campos entre leitura e edicao. Codigo nunca volta a ser editavel apos cadastro. */
    public void definirModoLeitura(boolean leitura) {
        boolean editavel = !leitura;
        campoNome.setEnabled(editavel);
        comboCategoria.setEnabled(editavel);
        campoPreco.setEnabled(editavel);
        campoEstoque.setEnabled(editavel);
        // Em edicao de produto existente o codigo permanece travado (identificador).
        campoCodigo.setEnabled(editavel && campoCodigo.getText().isBlank());
        btnSalvar.setVisible(editavel);
        btnEditar.setVisible(leitura);
    }

    /** Modo novo cadastro: tudo editavel, sem botao Editar. */
    public void definirModoNovo() {
        definirModoLeitura(false);
        campoCodigo.setEnabled(true);
        btnEditar.setVisible(false);
    }

    public String getCodigo() { return campoCodigo.getText().trim(); }
    public String getNome() { return campoNome.getText().trim(); }
    public Categoria getCategoria() { return (Categoria) comboCategoria.getSelectedItem(); }
    public String getPreco() { return campoPreco.getText().trim(); }
    public String getEstoque() { return campoEstoque.getText().trim(); }

    public void setMensagemErro(String msg) {
        lblErro.setForeground(Color.RED);
        lblErro.setText(msg == null ? " " : msg);
    }
    public void setMensagemSucesso(String msg) {
        lblErro.setForeground(new Color(0, 128, 0));
        lblErro.setText(msg);
    }

    public JButton getBtnSalvar() { return btnSalvar; }
    public JButton getBtnEditar() { return btnEditar; }
    public JButton getBtnFechar() { return btnFechar; }
}
