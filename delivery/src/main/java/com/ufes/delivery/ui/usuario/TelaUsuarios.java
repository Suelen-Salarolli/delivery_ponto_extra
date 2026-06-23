package com.ufes.delivery.ui.usuario;

import com.ufes.delivery.model.PerfilUsuario;
import com.ufes.delivery.model.SituacaoUsuario;
import com.ufes.delivery.model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TelaUsuarios extends JFrame {

    private JTextField campoBusca;
    private JButton btnBuscar;
    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private JButton btnAutorizar;
    private JButton btnDesautorizar;
    private JButton btnExcluir;
    private JButton btnNovo;
    private JButton btnFechar;
    private JLabel lblStatus;

    // Dados carregados na tabela (índice = linha)
    private List<Usuario> usuariosCarregados = new ArrayList<>();

    public TelaUsuarios() {
        super("Usuarios");
        construirUI();
        setSize(820, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void construirUI() {
        setLayout(new BorderLayout(8, 8));

        // --- Painel de busca ---
        JPanel painelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        painelBusca.setBorder(BorderFactory.createTitledBorder("Busca de Usuarios"));
        painelBusca.add(new JLabel("Nome:"));
        campoBusca = new JTextField(28);
        painelBusca.add(campoBusca);
        btnBuscar = new JButton("Buscar");
        painelBusca.add(btnBuscar);

        // --- Tabela ---
        String[] colunas = {"Sel.", "Nome de usuario", "Nome", "Autorizado", "Perfil", "Situacao"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override public Class<?> getColumnClass(int col) {
                return col == 0 || col == 3 ? Boolean.class : String.class;
            }
            @Override public boolean isCellEditable(int row, int col) {
                return col == 0 || col == 4; // seleção e perfil editáveis
            }
        };

        tabela = new JTable(modeloTabela);
        tabela.setRowHeight(24);
        tabela.getColumnModel().getColumn(0).setMaxWidth(40);
        tabela.getColumnModel().getColumn(3).setMaxWidth(80);

        // Combo de perfil na coluna 4
        JComboBox<String> comboPerfil = new JComboBox<>(
            new String[]{PerfilUsuario.ADMINISTRADOR.getDescricao(), PerfilUsuario.ATENDENTE.getDescricao()});
        TableColumn colPerfil = tabela.getColumnModel().getColumn(4);
        colPerfil.setCellEditor(new DefaultCellEditor(comboPerfil));

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createTitledBorder("Usuarios"));

        // --- Botões ---
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnAutorizar = new JButton("Autorizar");
        btnDesautorizar = new JButton("Desautorizar");
        btnExcluir = new JButton("Excluir");
        btnNovo = new JButton("Novo");
        btnFechar = new JButton("Fechar");
        painelBotoes.add(btnAutorizar);
        painelBotoes.add(btnDesautorizar);
        painelBotoes.add(btnExcluir);
        painelBotoes.add(btnNovo);
        painelBotoes.add(btnFechar);

        lblStatus = new JLabel(" ");
        lblStatus.setFont(lblStatus.getFont().deriveFont(Font.PLAIN, 11f));
        JPanel painelRodape = new JPanel(new BorderLayout());
        painelRodape.add(lblStatus, BorderLayout.WEST);
        painelRodape.add(painelBotoes, BorderLayout.EAST);

        add(painelBusca, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(painelRodape, BorderLayout.SOUTH);
    }

    /** Preenche a tabela com a lista de usuários */
    public void carregarUsuarios(List<Usuario> usuarios) {
        modeloTabela.setRowCount(0);
        usuariosCarregados = new ArrayList<>(usuarios);
        for (Usuario u : usuarios) {
            modeloTabela.addRow(new Object[]{
                false,
                u.getUsername(),
                u.getNome(),
                u.isAutorizado(),
                u.getPerfil().getDescricao(),
                u.getSituacao().getDescricao()
            });
        }
    }

    /** Retorna os usuários cuja checkbox (col 0) está marcada */
    public List<Usuario> getUsuariosSelecionados() {
        // Confirma edição em andamento
        if (tabela.isEditing()) tabela.getCellEditor().stopCellEditing();

        List<Usuario> selecionados = new ArrayList<>();
        for (int i = 0; i < modeloTabela.getRowCount(); i++) {
            Boolean sel = (Boolean) modeloTabela.getValueAt(i, 0);
            if (Boolean.TRUE.equals(sel)) {
                Usuario u = usuariosCarregados.get(i);
                // Sincroniza perfil editado na tabela
                String perfilTabela = (String) modeloTabela.getValueAt(i, 4);
                u.setPerfil(PerfilUsuario.fromDescricao(perfilTabela));
                selecionados.add(u);
            }
        }
        return selecionados;
    }

    public String getFiltroBusca() { return campoBusca.getText().trim(); }

    public void setMensagem(String msg, boolean erro) {
        lblStatus.setText(msg);
        lblStatus.setForeground(erro ? Color.RED : new Color(0, 120, 0));
    }

    public JButton getBtnBuscar() { return btnBuscar; }
    public JButton getBtnAutorizar() { return btnAutorizar; }
    public JButton getBtnDesautorizar() { return btnDesautorizar; }
    public JButton getBtnExcluir() { return btnExcluir; }
    public JButton getBtnNovo() { return btnNovo; }
    public JButton getBtnFechar() { return btnFechar; }
    public JTextField getCampoBusca() { return campoBusca; }
}
