package com.ufes.delivery.ui.painel;

import com.ufes.delivery.configuracao.ConfiguracaoService;
import com.ufes.delivery.factory.PedidoModuleFactory;
import com.ufes.delivery.model.Sessao;
import com.ufes.delivery.model.Usuario;
import com.ufes.delivery.model.cadastro.PedidoResumo;
import com.ufes.delivery.service.ClienteService;
import com.ufes.delivery.service.PainelService;
import com.ufes.delivery.service.ProdutoService;
import com.ufes.delivery.service.UsuarioService;
import com.ufes.delivery.ui.cliente.TelaBuscaClientes;
import com.ufes.delivery.ui.cliente.TelaBuscaClientesPresenter;
import com.ufes.delivery.ui.cliente.TelaCliente;
import com.ufes.delivery.ui.cliente.TelaClientePresenter;
import com.ufes.delivery.ui.produto.TelaBuscaProdutos;
import com.ufes.delivery.ui.produto.TelaBuscaProdutosPresenter;
import com.ufes.delivery.ui.produto.TelaProduto;
import com.ufes.delivery.ui.produto.TelaProdutoPresenter;
import com.ufes.delivery.dao.MovimentacaoEstoqueDAO;
import com.ufes.delivery.service.EstoqueService;
import com.ufes.delivery.ui.estoque.TelaMovimentacaoEstoque;
import com.ufes.delivery.ui.estoque.TelaMovimentacaoEstoquePresenter;
import com.ufes.delivery.ui.pedido.TelaPedido;
import com.ufes.delivery.ui.pedido.TelaPedidoPresenter;
import com.ufes.delivery.auditoria.IAuditoriaService;
import com.ufes.delivery.service.PagamentoService;
import com.ufes.delivery.ui.usuario.TelaUsuarios;
import com.ufes.delivery.ui.usuario.TelaUsuariosPresenter;

import javax.swing.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Presenter do painel operacional (US04). Carrega metricas/pedidos da data de
 * operacao, monta a barra de status e roteia os comandos do menu Operacao.
 */
public class TelaPainelPresenter {

    private static final DateTimeFormatter LOGIN_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final TelaPainel view;
    private final PainelService painelService;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;
    private final UsuarioService usuarioService;
    private final Usuario usuarioLogado;
    private final EstoqueService estoqueService;
    private final IAuditoriaService auditoria;

    public TelaPainelPresenter(TelaPainel view,
                               PainelService painelService,
                               ClienteService clienteService,
                               ProdutoService produtoService,
                               UsuarioService usuarioService,
                               Usuario usuarioLogado,
                               IAuditoriaService auditoria) {
        this.view = view;
        this.painelService = painelService;
        this.clienteService = clienteService;
        this.produtoService = produtoService;
        this.usuarioService = usuarioService;
        this.usuarioLogado = usuarioLogado;
        this.auditoria = auditoria;
        this.estoqueService = new EstoqueService(new MovimentacaoEstoqueDAO(), auditoria);
        configurar();
        vincularEventos();
        atualizarPainel();
    }

    private void configurar() {
        // US03 — menu de administracao visivel apenas para Administrador.
        view.setMenuAdministracaoVisivel(usuarioLogado.isAdministrador());

        // Barra de status (US04 Cenario 3).
        var login = Sessao.getInstance().getDataHoraLogin();
        view.setBarraStatus(
            usuarioLogado.getUsername(),
            login == null ? "" : login.format(LOGIN_FMT),
            usuarioLogado.getPerfil().getDescricao());
    }

    private void vincularEventos() {
        view.getMenuBuscarClientes().addActionListener(e -> abrirBuscarClientes());
        view.getMenuNovoCliente().addActionListener(e -> abrirNovoCliente());
        view.getMenuBuscarProdutos().addActionListener(e -> abrirBuscarProdutos());
        view.getMenuNovoProduto().addActionListener(e -> abrirNovoProduto());
        view.getMenuUsuarios().addActionListener(e -> abrirUsuarios());
        view.getMenuNovoPedido().addActionListener(e -> abrirNovoPedido());
        view.getMenuMovimentacaoEstoque().addActionListener(e -> abrirMovimentacaoEstoque());
        view.getBtnVisualizar().addActionListener(e -> visualizarPedido());
    }

    private void atualizarPainel() {
        LocalDate data = ConfiguracaoService.getDataOperacao();
        view.setDataOperacao(data);
        try {
            view.setMetricas(painelService.getMetricas(data));
            view.carregarPedidos(painelService.getPedidos(data));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view,
                "Erro ao carregar o painel: " + ex.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirBuscarClientes() {
        TelaBuscaClientes tela = new TelaBuscaClientes();
        new TelaBuscaClientesPresenter(tela, clienteService);
        tela.setVisible(true);
    }

    private void abrirNovoCliente() {
        TelaCliente tela = new TelaCliente(view);
        new TelaClientePresenter(tela, clienteService, null);
        tela.setVisible(true);
    }

    private void abrirBuscarProdutos() {
        TelaBuscaProdutos tela = new TelaBuscaProdutos();
        new TelaBuscaProdutosPresenter(tela, produtoService);
        tela.setVisible(true);
    }

    private void abrirNovoProduto() {
        TelaProduto tela = new TelaProduto(view);
        new TelaProdutoPresenter(tela, produtoService, null);
        tela.setVisible(true);
    }

    private void abrirNovoPedido() {
        TelaPedido tela = new TelaPedido(view);
        new TelaPedidoPresenter(
            tela,
            clienteService,
            produtoService,
            PedidoModuleFactory.criarPedidoService(auditoria),
            PedidoModuleFactory.criarPagamentoService(auditoria),
            this::atualizarPainel
        );
        tela.setVisible(true);
    }

    private void abrirUsuarios() {
        if (!usuarioLogado.isAdministrador()) {
            JOptionPane.showMessageDialog(view,
                "Funcionalidade restrita ao Administrador",
                "Acesso negado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        TelaUsuarios tela = new TelaUsuarios();
        new TelaUsuariosPresenter(tela, usuarioService);
        tela.setVisible(true);
    }

    private void visualizarPedido() {
        PedidoResumo p = view.getPedidoSelecionado();
        if (p == null) {
            JOptionPane.showMessageDialog(view,
                "Selecione um pedido para visualizar",
                "Atencao", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        // AIDEV-NOTE: detalhe completo do pedido (itens, cupom, pagamento) chega na
        // Fase 3/4 (US09/US10). Aqui exibimos o resumo persistido do read model.
        String detalhe = "Pedido: " + p.getCodigo()
            + "\nCliente: " + p.getClienteNome()
            + "\nData do pedido: " + p.getDataPedido()
            + "\nData de conclusao: " + (p.getDataConclusao() == null ? "-" : p.getDataConclusao())
            + "\nEstado: " + p.getEstado().getDescricao()
            + "\nValor total: " + com.ufes.delivery.util.MoedaUtil.formatar(p.getValorTotal());
        JOptionPane.showMessageDialog(view, detalhe,
            "Pedido " + p.getCodigo(), JOptionPane.INFORMATION_MESSAGE);
    }


    private void abrirMovimentacaoEstoque() {
        if (!usuarioLogado.isAdministrador()) {
            JOptionPane.showMessageDialog(view,
                "Movimentacao de estoque e restrita ao Administrador",
                "Acesso negado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        TelaMovimentacaoEstoque tela = new TelaMovimentacaoEstoque(view);
        new TelaMovimentacaoEstoquePresenter(tela, produtoService, estoqueService);
        tela.setVisible(true);
    }

}
