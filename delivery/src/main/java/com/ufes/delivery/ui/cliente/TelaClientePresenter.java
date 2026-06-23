package com.ufes.delivery.ui.cliente;

import com.ufes.delivery.model.cadastro.Cliente;
import com.ufes.delivery.model.cadastro.Endereco;
import com.ufes.delivery.model.cadastro.Uf;
import com.ufes.delivery.service.ClienteService;

import java.util.List;

/**
 * Presenter de cadastro/edicao de cliente (US06). Monta o agregado Cliente a
 * partir das linhas da view e delega validacao/persistencia ao servico.
 */
public class TelaClientePresenter {

    private final TelaCliente view;
    private final ClienteService clienteService;
    private Cliente clienteEmEdicao; // null = novo

    public TelaClientePresenter(TelaCliente view, ClienteService clienteService, Cliente cliente) {
        this.view = view;
        this.clienteService = clienteService;
        this.clienteEmEdicao = cliente;
        vincularEventos();

        if (cliente == null) {
            view.definirModoNovo();
            view.adicionarLinhaEndereco(); // ja inicia com uma linha padrao
        } else {
            view.preencher(cliente);
        }
    }

    private void vincularEventos() {
        view.getBtnAdicionarEndereco().addActionListener(e -> view.adicionarLinhaEndereco());
        view.getBtnRemoverEndereco().addActionListener(e -> view.removerLinhaEnderecoSelecionada());
        view.getBtnSalvar().addActionListener(e -> salvar());
        view.getBtnEditar().addActionListener(e -> view.definirModoLeitura(false));
        view.getBtnFechar().addActionListener(e -> view.dispose());
    }

    private void salvar() {
        view.setMensagemErro(" ");
        try {
            if (view.getNome().isEmpty()) { view.setMensagemErro("Nome do cliente e obrigatorio"); return; }
            if (view.getCpf().isEmpty()) { view.setMensagemErro("CPF e obrigatorio"); return; }

            Cliente cliente = (clienteEmEdicao == null) ? new Cliente() : clienteEmEdicao;
            cliente.setNome(view.getNome());
            cliente.setCpf(view.getCpf());
            cliente.limparEnderecos();

            for (Object[] linha : view.getLinhasEndereco()) {
                cliente.adicionarEndereco(construirEndereco(linha));
            }

            if (clienteEmEdicao == null) {
                clienteEmEdicao = clienteService.cadastrar(cliente);
                view.preencher(clienteEmEdicao);
                view.setMensagemSucesso("Cliente cadastrado com sucesso!");
            } else {
                clienteService.atualizar(cliente);
                view.preencher(cliente);
                view.setMensagemSucesso("Cliente atualizado com sucesso!");
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            view.setMensagemErro(ex.getMessage());
        } catch (Exception ex) {
            view.setMensagemErro("Erro ao salvar: " + ex.getMessage());
        }
    }

    private Endereco construirEndereco(Object[] linha) {
        boolean padrao = Boolean.TRUE.equals(linha[TelaCliente.COL_PADRAO]);
        String logradouro = texto(linha[TelaCliente.COL_LOGRADOURO]);
        String numero = texto(linha[TelaCliente.COL_NUMERO]);
        String complemento = texto(linha[TelaCliente.COL_COMPLEMENTO]);
        String bairro = texto(linha[TelaCliente.COL_BAIRRO]);
        String cidade = texto(linha[TelaCliente.COL_CIDADE]);
        Uf uf = lerUf(linha[TelaCliente.COL_UF]);
        String cep = texto(linha[TelaCliente.COL_CEP]);
        return new Endereco(logradouro, numero, complemento, bairro, cidade, uf, cep, padrao);
    }

    private String texto(Object valor) {
        return valor == null ? "" : valor.toString().trim();
    }

    private Uf lerUf(Object valor) {
        if (valor instanceof Uf u) return u;
        return Uf.de(texto(valor));
    }
}
