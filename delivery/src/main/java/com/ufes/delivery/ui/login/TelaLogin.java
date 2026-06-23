package com.ufes.delivery.ui.login;

import javax.swing.*;
import java.awt.*;

public class TelaLogin extends JDialog {

    private JTextField campoUsername;
    private JPasswordField campoSenha;
    private JButton btnAcessar;
    private JButton btnCancelar;
    private JButton btnCadastrar;
    private JLabel lblErro;

    public TelaLogin(Frame owner) {
        super(owner, "Login", true);
        construirUI();
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void construirUI() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Dados de Acesso"));
        painel.setPreferredSize(new Dimension(400, 180));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        painel.add(new JLabel("Nome de usuario:"), gbc);
        campoUsername = new JTextField(22);
        // Força lowercase e sem espaço ao digitar
        campoUsername.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { normalizar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {}
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
            private void normalizar() {
                SwingUtilities.invokeLater(() -> {
                    String txt = campoUsername.getText();
                    String limpo = txt.toLowerCase().replace(" ", "");
                    if (!txt.equals(limpo)) {
                        int caret = campoUsername.getCaretPosition();
                        campoUsername.setText(limpo);
                        campoUsername.setCaretPosition(Math.min(caret, limpo.length()));
                    }
                });
            }
        });
        gbc.gridx = 1; gbc.weightx = 1;
        painel.add(campoUsername, gbc);

        // Senha
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        painel.add(new JLabel("Senha:"), gbc);
        campoSenha = new JPasswordField(22);
        gbc.gridx = 1; gbc.weightx = 1;
        painel.add(campoSenha, gbc);

        // Erro
        lblErro = new JLabel(" ");
        lblErro.setForeground(Color.RED);
        lblErro.setFont(lblErro.getFont().deriveFont(Font.PLAIN, 11f));
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        painel.add(lblErro, gbc);

        // Botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        btnAcessar = new JButton("Acessar");
        btnCancelar = new JButton("Cancelar");
        btnCadastrar = new JButton("Cadastrar usuario");
        painelBotoes.add(btnAcessar);
        painelBotoes.add(btnCancelar);
        painelBotoes.add(btnCadastrar);

        getRootPane().setDefaultButton(btnAcessar);

        JPanel root = new JPanel(new BorderLayout());
        root.add(painel, BorderLayout.CENTER);
        root.add(painelBotoes, BorderLayout.SOUTH);
        setContentPane(root);
    }

    // --- Getters para o Presenter ---
    public String getUsername() { return campoUsername.getText().trim(); }
    public String getSenha() { return new String(campoSenha.getPassword()); }

    public void setMensagemErro(String msg) { lblErro.setText(msg == null ? " " : msg); }
    public void limparSenha() { campoSenha.setText(""); }

    public JButton getBtnAcessar() { return btnAcessar; }
    public JButton getBtnCancelar() { return btnCancelar; }
    public JButton getBtnCadastrar() { return btnCadastrar; }
}
