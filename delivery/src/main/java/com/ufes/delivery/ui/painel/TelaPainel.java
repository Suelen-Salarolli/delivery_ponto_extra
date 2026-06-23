package com.ufes.delivery.ui.painel;

import com.ufes.delivery.model.cadastro.PedidoResumo;
import com.ufes.delivery.service.PainelService.Metricas;
import com.ufes.delivery.util.MoedaUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Painel operacional (US04): data de operacao em destaque, metricas por estado,
 * lista de pedidos, menu Operacao e barra de status. View passiva.
 */
public class TelaPainel extends JFrame {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JLabel lblDataOperacao;
    private final java.util.Map<String, JLabel> valoresMetricas = new java.util.HashMap<>();
    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private JButton btnVisualizar;
    private JLabel lblStatus;

    // Menu Operacao
    private JMenuItem menuNovoPedido;
    private JMenuItem menuBuscarProdutos;
    private JMenuItem menuNovoProduto;
    private JMenuItem menuMovimentacaoEstoque;
    private JMenuItem menuNovoCliente;
    private JMenuItem menuBuscarClientes;
    // Menu Administracao
    private JMenu menuAdministracao;
    private JMenuItem menuUsuarios;

    private List<PedidoResumo> pedidosCarregados = new ArrayList<>();

    public TelaPainel() {
        super("Painel Operacional - Delivery");
        construirMenu();
        construirUI();
        setSize(960, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void construirMenu() {
        JMenuBar barra = new JMenuBar();

        JMenu menuOperacao = new JMenu("Operacao");
        menuNovoPedido = new JMenuItem("Novo pedido");
        menuBuscarProdutos = new JMenuItem("Buscar produtos");
        menuNovoProduto = new JMenuItem("Novo produto");
        menuMovimentacaoEstoque = new JMenuItem("Movimentacao de estoque");
        menuNovoCliente = new JMenuItem("Novo cliente");
        menuBuscarClientes = new JMenuItem("Buscar clientes");
        menuOperacao.add(menuNovoPedido);
        menuOperacao.add(menuBuscarProdutos);
        menuOperacao.add(menuNovoProduto);
        menuOperacao.add(menuMovimentacaoEstoque);
        menuOperacao.add(menuNovoCliente);
        menuOperacao.add(menuBuscarClientes);

        menuAdministracao = new JMenu("Administracao");
        menuUsuarios = new JMenuItem("Usuarios");
        menuAdministracao.add(menuUsuarios);

        barra.add(menuOperacao);
        barra.add(menuAdministracao);
        setJMenuBar(barra);
    }

    private void construirUI() {
        setLayout(new BorderLayout(8, 8));

        // Topo: data de operacao + metricas
        JPanel topo = new JPanel(new BorderLayout(8, 8));
        lblDataOperacao = new JLabel("Data de operacao: --/--/----");
        lblDataOperacao.setFont(lblDataOperacao.getFont().deriveFont(Font.BOLD, 20f));
        lblDataOperacao.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        topo.add(lblDataOperacao, BorderLayout.NORTH);

        JPanel painelMetricas = new JPanel(new GridLayout(0, 7, 6, 6));
        painelMetricas.setBorder(BorderFactory.createTitledBorder("Metricas do dia"));
        adicionarMetrica(painelMetricas, "Pedidos do dia");
        adicionarMetrica(painelMetricas, "Novos");
        adicionarMetrica(painelMetricas, "Aguardando pagamento");
        adicionarMetrica(painelMetricas, "Em preparo");
        adicionarMetrica(painelMetricas, "Aguardando entrega");
        adicionarMetrica(painelMetricas, "Em transito");
        adicionarMetrica(painelMetricas, "Entregues hoje");
        topo.add(painelMetricas, BorderLayout.CENTER);

        // Centro: tabela de pedidos
        String[] colunas = {"Pedido", "Cliente", "Data do pedido", "Data de conclusao", "Estado", "Valor total"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabela = new JTable(modeloTabela);
        tabela.setRowHeight(24);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createTitledBorder("Pedidos da data de operacao"));

        JPanel painelAcoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        btnVisualizar = new JButton("Visualizar");
        painelAcoes.add(btnVisualizar);

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(scroll, BorderLayout.CENTER);
        centro.add(painelAcoes, BorderLayout.SOUTH);

        // Barra de status
        lblStatus = new JLabel(" ");
        lblStatus.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        lblStatus.setOpaque(true);
        lblStatus.setBackground(new Color(238, 238, 238));

        add(topo, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(lblStatus, BorderLayout.SOUTH);
    }

    private void adicionarMetrica(JPanel painel, String titulo) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 210, 210)),
            BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        JLabel lblTitulo = new JLabel("<html><center>" + titulo + "</center></html>", SwingConstants.CENTER);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.PLAIN, 11f));
        JLabel lblValor = new JLabel("0", SwingConstants.CENTER);
        lblValor.setFont(lblValor.getFont().deriveFont(Font.BOLD, 22f));
        card.add(lblValor, BorderLayout.CENTER);
        card.add(lblTitulo, BorderLayout.SOUTH);
        painel.add(card);
        valoresMetricas.put(titulo, lblValor);
    }

    public void setDataOperacao(LocalDate data) {
        lblDataOperacao.setText("Data de operacao: " + data.format(DATA));
    }

    public void setMetricas(Metricas m) {
        valoresMetricas.get("Pedidos do dia").setText(String.valueOf(m.pedidosDoDia()));
        valoresMetricas.get("Novos").setText(String.valueOf(m.novos()));
        valoresMetricas.get("Aguardando pagamento").setText(String.valueOf(m.aguardandoPagamento()));
        valoresMetricas.get("Em preparo").setText(String.valueOf(m.emPreparo()));
        valoresMetricas.get("Aguardando entrega").setText(String.valueOf(m.aguardandoEntrega()));
        valoresMetricas.get("Em transito").setText(String.valueOf(m.emTransito()));
        valoresMetricas.get("Entregues hoje").setText(String.valueOf(m.entreguesHoje()));
    }

    public void carregarPedidos(List<PedidoResumo> pedidos) {
        modeloTabela.setRowCount(0);
        pedidosCarregados = new ArrayList<>(pedidos);
        for (PedidoResumo p : pedidos) {
            modeloTabela.addRow(new Object[]{
                p.getCodigo(),
                p.getClienteNome(),
                p.getDataPedido().format(DATA),
                p.getDataConclusao() == null ? "" : p.getDataConclusao().format(DATA),
                p.getEstado().getDescricao(),
                MoedaUtil.formatar(p.getValorTotal())
            });
        }
    }

    public PedidoResumo getPedidoSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) return null;
        return pedidosCarregados.get(linha);
    }

    public void setBarraStatus(String usuario, String login, String tipo) {
        lblStatus.setText("Usuario logado: " + usuario + "    |    Login: " + login + "    |    Tipo: " + tipo);
    }

    public void setMenuAdministracaoVisivel(boolean visivel) {
        menuAdministracao.setVisible(visivel);
    }

    public JMenuItem getMenuNovoPedido() { return menuNovoPedido; }
    public JMenuItem getMenuBuscarProdutos() { return menuBuscarProdutos; }
    public JMenuItem getMenuNovoProduto() { return menuNovoProduto; }
    public JMenuItem getMenuMovimentacaoEstoque() { return menuMovimentacaoEstoque; }
    public JMenuItem getMenuNovoCliente() { return menuNovoCliente; }
    public JMenuItem getMenuBuscarClientes() { return menuBuscarClientes; }
    public JMenuItem getMenuUsuarios() { return menuUsuarios; }
    public JButton getBtnVisualizar() { return btnVisualizar; }
    public JTable getTabela() { return tabela; }
}
