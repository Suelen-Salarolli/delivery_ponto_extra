package com.ufes.delivery.ui.usuario;

import javax.swing.*;
import java.awt.*;

public class TelaCadastroUsuario extends JDialog {

    private JTextField campoNome;
    private JTextField campoUsername;
    private JPasswordField campoSenha;
    private JLabel lblErro;
    private JButton btnConfirmar;
    private JButton btnCancelar;

    public TelaCadastroUsuario(Frame owner) {
        super(owner, "Cadastro de Usuario", true);
        construirUI();
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void construirUI() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Dados do Usuario"));
        painel.setPreferredSize(new Dimension(420, 190));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nome
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        painel.add(new JLabel("Nome:"), gbc);
        campoNome = new JTextField(24);
        gbc.gridx = 1; gbc.weightx = 1;
        painel.add(campoNome, gbc);

        // Username
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        painel.add(new JLabel("Nome de usuario:"), gbc);
        campoUsername = new JTextField(24);
        campoUsername.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { normalizar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {}
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
            private void normalizar() {
                SwingUtilities.invokeLater(() -> {
                    String txt = campoUsername.getText();
                    String limpo = txt.toLowerCase().replace(" ", "");
                    if (!txt.equals(limpo)) {
                        campoUsername.setText(limpo);
                    }
                });
            }
        });
        gbc.gridx = 1; gbc.weightx = 1;
        painel.add(campoUsername, gbc);

        // Senha
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        painel.add(new JLabel("Senha:"), gbc);
        campoSenha = new JPasswordField(24);
        gbc.gridx = 1; gbc.weightx = 1;
        painel.add(campoSenha, gbc);

        // Erro
        lblErro = new JLabel(" ");
        lblErro.setForeground(Color.RED);
        lblErro.setFont(lblErro.getFont().deriveFont(Font.PLAIN, 11f));
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        painel.add(lblErro, gbc);

        // Botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        btnConfirmar = new JButton("Confirmar");
        btnCancelar = new JButton("Cancelar");
        painelBotoes.add(btnConfirmar);
        painelBotoes.add(btnCancelar);

        getRootPane().setDefaultButton(btnConfirmar);

        JPanel root = new JPanel(new BorderLayout());
        root.add(painel, BorderLayout.CENTER);
        root.add(painelBotoes, BorderLayout.SOUTH);
        setContentPane(root);
    }

    public String getNome() { return campoNome.getText().trim(); }
    public String getUsername() { return campoUsername.getText().trim(); }
    public String getSenha() { return new String(campoSenha.getPassword()); }

    public void setMensagemErro(String msg) { lblErro.setText(msg == null ? " " : msg); }
    public void setMensagemSucesso(String msg) {
        lblErro.setForeground(new Color(0, 128, 0));
        lblErro.setText(msg);
    }

    public JButton getBtnConfirmar() { return btnConfirmar; }
    public JButton getBtnCancelar() { return btnCancelar; }
}
