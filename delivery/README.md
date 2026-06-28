#  Delivery — POC (Proof of Concept)

Sistema desktop de gerenciamento de pedidos, clientes, produtos e estoque para operações de delivery.  

---

##  Como Compilar e Executar

### Pré-requisitos

- **JDK 21** ou superior instalado e configurado no `JAVA_HOME`
- **Apache Maven 3.x** instalado e no `PATH`
- Conexão com a internet na primeira execução (para download de dependências via Maven/JitPack)

### Compilação e Execução

```bash
# 1. Clone o repositório
git clone <url-do-repositorio>
cd delivery

# 2. Compile o projeto
mvn clean compile

# 3. Execute a aplicação
mvn exec:java
```

Na primeira execução, o sistema:
- Cria automaticamente o banco de dados `delivery.db`
- Semeia dados de demonstração (8 pedidos para o painel operacional)
- Abre a tela de login

> **Primeiro acesso:** cadastre um usuário — ele será automaticamente definido como **Administrador** com situação **Autorizado**.

---

##  Índice

- [Visão Geral](#-visão-geral)
- [Tecnologias](#-tecnologias)
- [Arquitetura](#-arquitetura)
- [Estrutura de Pacotes](#-estrutura-de-pacotes)
- [Funcionalidades (User Stories)](#-funcionalidades-user-stories)
- [Padrões de Projeto](#-padrões-de-projeto)
- [Modelo de Domínio](#-modelo-de-domínio)
- [Regras Transversais](#-regras-transversais)
- [Auditoria (CR2)](#-auditoria-cr2)
- [Dados de Demonstração](#-dados-de-demonstração)
- [Banco de Dados](#-banco-de-dados)

---

##  Visão Geral

O sistema implementa o fluxo operacional completo de um serviço de delivery:

1. **Autenticação e controle de acesso** por perfil (Administrador / Atendente)
2. **Cadastro e manutenção** de usuários, clientes (com endereços) e produtos
3. **Movimentação de estoque** com entrada por nota fiscal e ajustes justificados
4. **Criação de pedidos** com itens, cupons de desconto e cálculo de taxas de entrega
5. **Simulação de pagamento** com resultado probabilístico (50% aprovado/reprovado)
6. **Painel operacional** com métricas por estado do pedido
7. **Registro de auditoria** para rastreabilidade de todas as operações

---

##  Tecnologias

| Tecnologia | Versão | Uso |
|---|---|---|
| **Java** | 21 | Linguagem principal |
| **Swing** | JDK 21 | Interface gráfica (desktop) |
| **SQLite** | 3.45.3 | Banco de dados embarcado |
| **Maven** | 3.x | Gerenciamento de dependências e build |
| **JitPack** | — | Distribuição da biblioteca externa de auditoria |
| **SistemaLogAuditoria** | CR2 | Biblioteca externa de logs (JSON / CSV / XML) |


---

##  Arquitetura

O sistema segue a arquitetura **MVP Passive View** com separação em camadas:

```
┌─────────────────────────────────────────────┐
│                   UI (View)                 │
│         Swing JDialog / JFrame              │
│    Passiva: apenas getters/setters          │
├─────────────────────────────────────────────┤
│                 Presenter                   │
│  Mediador: vincula eventos da view aos      │
│  serviços de negócio. Toda lógica de        │
│  interação vive aqui.                       │
├─────────────────────────────────────────────┤
│                  Service                    │
│  Regras de negócio e validação de domínio   │
├─────────────────────────────────────────────┤
│               DAO / Repository              │
│  Persistência SQLite e repositório em       │
│  memória (cupons)                           │
├─────────────────────────────────────────────┤
│            Model (Domínio Rico)             │
│  Entidades, enums, State pattern,           │
│  validações de invariantes                  │
└─────────────────────────────────────────────┘
```

**Princípio fundamental:** Views não conhecem serviços. Presenters não conhecem SQL. Services não conhecem Swing.

---

##  Estrutura de Pacotes

```
com.ufes.delivery
├── Main.java                          # Ponto de entrada, composição de dependências
│
├── auditoria/                         # Módulo de auditoria (CR2)
│   ├── IAuditoriaService.java         #   Interface de serviço de auditoria
│   ├── AuditoriaManager.java          #   Gerenciador central (Observer + Strategy)
│   └── TipoLog.java                  #   Enum: JSONL, CSV, XML
│
├── configuracao/
│   └── ConfiguracaoService.java       # Data operacional e taxa de entrega padrão
│
├── dao/                               # Camada de acesso a dados (SQLite)
│   ├── ClienteDAO.java
│   ├── MovimentacaoEstoqueDAO.java
│   ├── PagamentoDAO.java             #   Transação atômica: pagamento + baixa + estado
│   ├── PedidoDAO.java
│   ├── PedidoResumoDAO.java
│   ├── ProdutoDAO.java
│   └── UsuarioDAO.java
│
├── db/                                # Infraestrutura de banco de dados
│   ├── ConexaoDB.java                 #   Conexão SQLite, DDL das tabelas
│   └── DemoSeed.java                 #   Dados demo para o painel (idempotente)
│
├── desconto/                          # Cálculo de descontos
│   └── entrega/                       #   Estratégias de desconto na taxa de entrega
│       ├── DescontoEntregaStrategy.java
│       ├── DescontoEntregaPorBairro.java
│       ├── DescontoEntregaPorCategoria.java
│       ├── DescontoEntregaPorValorPedido.java
│       └── CalculadoraDescontoEntregaService.java
│
├── factory/
│   └── PedidoModuleFactory.java       # Factory para módulo Pedido + Pagamento
│
├── model/                             # Entidades e objetos de domínio
│   ├── Sessao.java                    #   Singleton da sessão autenticada
│   ├── Usuario.java                   #   Usuário com validações de nome/username
│   ├── PerfilUsuario.java             #   Enum: Administrador, Atendente
│   ├── SituacaoUsuario.java           #   Enum: Autorizado, Pendente, Não autorizado
│   ├── CupomDescontoPedido.java       #   Cupom com período de validade
│   ├── cadastro/
│   │   ├── Cliente.java               #     Cliente com 1-3 endereços, CPF validado
│   │   ├── Endereco.java              #     Endereço de entrega com UF e CEP
│   │   ├── Produto.java               #     Produto com BigDecimal para preço
│   │   ├── Categoria.java             #     Enum de categorias de produto
│   │   ├── Uf.java                    #     Enum com 27 UFs brasileiras
│   │   ├── EstadoPedido.java          #     Enum do ciclo de vida do pedido
│   │   ├── PedidoResumo.java          #     DTO para listagem no painel
│   │   └── MovimentacaoEstoque.java   #     Registro de movimentação
│   ├── pagamento/
│   │   └── ResultadoPagamento.java    #     Resultado imutável da simulação
│   └── pedido/
│       ├── PedidoCadastro.java        #     Agregado do pedido (modelo rico)
│       ├── PedidoItem.java            #     Item do pedido com subtotal
│       └── estado/                    #     Padrão State para transições
│           ├── EstadoPedidoState.java
│           ├── NovoState.java
│           ├── AguardandoPagamentoState.java
│           ├── EmPreparoState.java
│           ├── AguardandoEntregaState.java
│           ├── EmTransitoState.java
│           └── EntregueState.java
│
├── repository/                        # Repositório de cupons
│   ├── ICupomRepository.java
│   └── CupomRepositoryEmMemoria.java  #   Cupons pré-configurados
│
├── service/                           # Regras de negócio
│   ├── AutenticacaoService.java       #   US01: login, sessão, validação
│   ├── UsuarioService.java            #   US02/US03: cadastro, autorização
│   ├── ClienteService.java            #   US05/US06: busca e manutenção
│   ├── ProdutoService.java            #   US07: cadastro e busca
│   ├── EstoqueService.java            #   US08: movimentação com prévia
│   ├── PedidoService.java             #   US09/US10: montagem e cupons
│   ├── PagamentoService.java          #   US10/US11: simulação e baixa
│   ├── PainelService.java             #   US04: métricas e listagem
│   ├── ISimuladorPagamento.java       #   Interface Strategy para simulação
│   └── SimuladorPagamentoAleatorio.java #  Implementação com probabilidade real
│
├── ui/                                # Camada de apresentação (Swing)
│   ├── login/
│   │   ├── TelaLogin.java             #   Tela de login
│   │   └── TelaLoginPresenter.java
│   ├── painel/
│   │   ├── TelaPainel.java            #   Painel operacional (tela principal)
│   │   └── TelaPainelPresenter.java
│   ├── usuario/
│   │   ├── TelaCadastroUsuario.java   #   Cadastro de credenciais
│   │   ├── TelaCadastroUsuarioPresenter.java
│   │   ├── TelaUsuarios.java          #   Gestão de usuários (admin)
│   │   └── TelaUsuariosPresenter.java
│   ├── cliente/
│   │   ├── TelaBuscaClientes.java     #   Busca por nome/CPF
│   │   ├── TelaBuscaClientesPresenter.java
│   │   ├── TelaCliente.java           #   Cadastro/edição com endereços
│   │   └── TelaClientePresenter.java
│   ├── produto/
│   │   ├── TelaBuscaProdutos.java     #   Busca por código/nome/categoria
│   │   ├── TelaBuscaProdutosPresenter.java
│   │   ├── TelaProduto.java           #   Cadastro/edição de produto
│   │   └── TelaProdutoPresenter.java
│   ├── estoque/
│   │   ├── TelaMovimentacaoEstoque.java  # Entrada/ajuste com prévia
│   │   └── TelaMovimentacaoEstoquePresenter.java
│   ├── pedido/
│   │   ├── TelaPedido.java            #   Criação de pedido com itens
│   │   └── TelaPedidoPresenter.java
│   └── pagamento/
│       ├── TelaPagamento.java         #   Resultado do pagamento (read-only)
│       └── TelaPagamentoPresenter.java
│
└── util/                              # Utilitários
    ├── CpfUtil.java                   #   Validação e formatação de CPF
    ├── MoedaUtil.java                 #   Formatação monetária brasileira
    └── SenhaUtil.java                 #   Hash SHA-256 e verificação
```

---

##  Funcionalidades (User Stories)

### US01 — Autenticar Usuário e Iniciar Sessão
- Campos: Nome de usuário (minúsculas + algarismos, 3-30 caracteres) e Senha (8-64 caracteres, mascarada)
- Validação de credenciais com mensagem genérica (não revela qual dado falhou)
- Bloqueio de usuários Pendentes ou Não autorizados (mensagem específica)
- Criação de sessão com dados para a barra de status
- Botão "Cadastrar usuário" abre formulário de cadastro

### US02 — Cadastrar Usuário
- Primeiro cadastro → perfil **Administrador**, situação **Autorizado** (automático)
- Cadastros posteriores → perfil **Atendente**, situação **Pendente**
- Username único em todo o sistema
- Senha armazenada como hash SHA-256

### US03 — Gerenciar Usuários e Autorizações
- Acesso exclusivo do perfil **Administrador**
- Busca por nome (case-insensitive)
- Seleção múltipla com checkboxes
- Ações: Autorizar, Desautorizar, Excluir, Novo
- Perfil editável por combo (Administrador / Atendente)

### US04 — Painel Operacional
- Data de operação em destaque (DD/MM/AAAA)
- 7 métricas: Pedidos do dia, Novos, Aguardando pagamento, Em preparo, Aguardando entrega, Em trânsito, Entregues hoje
- Tabela de pedidos com ação Visualizar
- **Menu Operação**: Novo pedido, Buscar produtos, Novo produto, Movimentação de estoque, Novo cliente, Buscar clientes
- Barra de status: Usuário logado, Login (DD/MM/AAAA HH:mm), Tipo

### US05 — Buscar Clientes
- Busca por **Nome** (2-120 caracteres, case-insensitive) ou **CPF** (validação por dígitos verificadores)
- CPF aceito com ou sem máscara
- Abrir cliente selecionado carrega dados completos na tela de manutenção

### US06 — Cadastrar, Editar e Visualizar Cliente
- Campos: Nome e CPF (único no cadastro)
- Tabela de até **3 endereços** de entrega com exatamente **1 padrão**
- Endereço: Logradouro, Número, Complemento (opcional), Bairro, Cidade, UF (enum), CEP (8 dígitos)
- Modos: inclusão, visualização, edição

### US07 — Buscar e Cadastrar Produto
- Busca por Código, Nome ou Categoria
- Código: inteiro positivo único
- Preço unitário: > R$ 0,00, máximo 2 casas decimais (BigDecimal)
- Estoque inicial: inteiro ≥ 0
- Categorias: Alimentação, Educação, Lazer, Entretenimento, Saúde, Vestuário, Outros

### US08 — Registrar Movimentação de Estoque
- Acesso exclusivo do **Administrador**
- Tipos: **Entrada** (exige nota fiscal) ou **Ajuste de estoque** (exige motivo)
- Tipo **Saída** não existe — baixa por venda é responsabilidade do `PagamentoService`
- Prévia do estoque resultante (somente visualização, sem persistir)
- Data da movimentação não pode ser posterior à data operacional
- Quantidade diferente de zero, estoque resultante ≥ 0
- Confirmação atômica: atualiza estoque + insere movimentação em transação única

### US09 — Criar Pedido, Administrar Itens e Aplicar Cupom
- Seleção de cliente e endereço de entrega (filtrado pelo cliente)
- Tabela de itens: Categoria, Produto, Preço unitário, Quantidade, Preço total
- Remoção de item por **menu de contexto** (botão direito → Excluir)
- Cupom de desconto com validação de existência e período de validade
- Totais calculados (não editáveis): Subtotal, Total de descontos, Desconto entrega, Taxa entrega final, Total do pedido
- Fórmula: `Total = Subtotal - Descontos + Taxa de entrega final`

### US10 — Validar Pedido, Simular Pagamento e Atualizar Estoque
- Verificação de disponibilidade de estoque no **instante** da confirmação
- Baixa de estoque em **transação atômica** (pagamento + baixa + mudança de estado)
- Insuficiência de estoque informa item e quantidade disponível
- Após aprovação: estado → **Aguardando entrega**

### US11 — Simular Resultado do Pagamento
- Resultado simulado: **50%** aprovado / **50%** reprovado
- Formas de pagamento: Open Finance, PIX chave, PIX QR Code, Cartão de crédito (**25%** cada)
- Prazo estimado de entrega: entre instante da aprovação e mesmo dia do mês seguinte
- Tela somente leitura com botão Fechar
- Aprovado: exibe verde, "Pagamento aprovado", "Pedido pronto para entrega"
- Reprovado: exibe vermelho, preserva pedido para nova tentativa
- `ISimuladorPagamento` substituível por implementação determinística em testes

### US12 — Registrar Eventos de Auditoria
- Eventos registrados: login, cadastro, autorização, desautorização, exclusão, manutenção de clientes/produtos, movimentação de estoque, criação de pedido, aplicação/rejeição de cupom, pagamento aprovado/reprovado, baixa de estoque, transição de estado
- Cada registro contém: usuário, data/hora, operação, recurso, resultado, justificativa
- **Proteção**: senhas e dados financeiros sensíveis nunca são registrados
- Modalidade única por execução (arquivo via biblioteca JitPack do CR2)

---

##  Padrões de Projeto

| Padrão | Tipo | Onde é Aplicado |
|---|---|---|
| **MVP Passive View** | Arquitetural | Todas as telas (`Tela*` + `Tela*Presenter`) |
| **State** | Comportamental | Ciclo de vida do pedido (`model/pedido/estado/`) |
| **Strategy** | Comportamental | Simulador de pagamento (`ISimuladorPagamento`), Descontos de entrega (`DescontoEntregaStrategy`) |
| **Observer** | Comportamental | Auditoria (`AuditoriaManager` + observadores de `IAuditoriaService`) |
| **Singleton** | Criacional | Sessão (`Sessao.getInstance()`), Conexão DB (`ConexaoDB`) |
| **Factory Method** | Criacional | Composição do módulo de pedido (`PedidoModuleFactory`) |
| **Repository** | Estrutural | Cupons (`ICupomRepository` / `CupomRepositoryEmMemoria`) |

---

##  Modelo de Domínio

### Perfis de Usuário
| Perfil | Permissões |
|---|---|
| **Administrador** | Acesso total: gerenciar usuários, produtos, estoque, pedidos, clientes |
| **Atendente** | Acesso operacional: pedidos, clientes, consulta de produtos |

### Situações do Usuário
| Situação | Descrição |
|---|---|
| **Autorizado** | Pode fazer login |
| **Pendente** | Aguardando autorização administrativa |
| **Não autorizado** | Bloqueado pelo administrador |

### Estados do Pedido
```
Novo → Aguardando entrega (após pagamento aprovado)
     → Em preparo → Em trânsito → Entregue
```

| Estado | Descrição |
|---|---|
| **Novo** | Pedido criado, aguardando pagamento |
| **Aguardando pagamento** | Pedido pronto para tentativa de pagamento |
| **Em preparo** | Pedido sendo preparado |
| **Aguardando entrega** | Pagamento aprovado, pronto para despacho |
| **Em trânsito** | Em rota de entrega |
| **Entregue** | Entregue ao cliente (data de conclusão preenchida) |

### Categorias de Produto
Alimentação, Educação, Lazer, Entretenimento, Saúde, Vestuário, Outros

---

##  Regras Transversais

| Regra | Implementação |
|---|---|
| **Dados monetários** | `BigDecimal` com até 2 casas decimais, formato brasileiro (R$ X.XXX,XX) |
| **Dados inteiros** | `int` para quantidades, estoque e identificadores |
| **Datas** | Formato DD/MM/AAAA, horários HH:mm |
| **Persistência** | Operações atômicas em transação única (sem persistência parcial) |
| **Mensagens** | Em português, associadas ao campo ou operação que gerou o erro |
| **Campos obrigatórios** | Valor semanticamente válido após trim; confirmação bloqueada se inválido |
| **Senha** | Mascarada na UI, armazenada como hash SHA-256, nunca em logs |
| **Estoque** | Verificado no instante da confirmação do pagamento |

---

##  Auditoria (CR2)

A auditoria utiliza a **biblioteca externa SistemaLogAuditoria** distribuída via JitPack (baseline do CR2 — Log de Auditoria).

### Modalidade de Persistência
O sistema opera com **uma única modalidade** configurada por execução:
- **JSONL** (padrão) → `delivery_auditoria.json`
- **CSV** → `delivery_auditoria.csv`
- **XML** → `delivery_auditoria.xml`

A modalidade é configurada no `AuditoriaManager` e pode ser alterada via `setFormatoLog(TipoLog)`.

### Estrutura do Registro
Cada evento contém:

| Campo | Descrição |
|---|---|
| `usuario` | Nome de usuário da sessão (ou "sistema" se sem sessão) |
| `data` | Data do evento (AAAA-MM-DD) |
| `hora` | Hora do evento |
| `operacao` | Nome da operação (ex: LOGIN, CADASTRO_PRODUTO, PAGAMENTO_APROVADO) |
| `recurso` | Identificação do recurso afetado (ex: usuario:admin, pedido:1001) |
| `resultado` | Resultado da operação (ex: Sucesso, Rejeitado, Aprovado) |
| `justificativa` | Contexto adicional (ex: motivo da rejeição, perfil atribuído) |

### Operações Auditadas
- `LOGIN` — autenticação (sucesso e rejeição)
- `CADASTRO_USUARIO` — criação de usuário
- `AUTORIZACAO_USUARIO` / `DESAUTORIZACAO_USUARIO` / `EXCLUSAO_USUARIO`
- `ALTERACAO_PERFIL` — mudança de perfil
- `CADASTRO_CLIENTE` / `ALTERACAO_CLIENTE`
- `CADASTRO_PRODUTO` / `ALTERACAO_PRODUTO`
- `MOVIMENTACAO_ESTOQUE` — entrada e ajuste
- `CADASTRO_PEDIDO` — criação de pedido
- `PAGAMENTO_APROVADO` / `PAGAMENTO_REPROVADO` / `PAGAMENTO_BLOQUEADO`
- `BAIXA_ESTOQUE` — baixa por item após pagamento aprovado
- `TRANSICAO_ESTADO` — mudança de estado do pedido

---

##  Dados de Demonstração

Na primeira execução, o `DemoSeed` cria automaticamente:
- **1 cliente demo** com endereço
- **Produtos demo** em diversas categorias
- **8 pedidos** na data de operação, em estados variados para validar o painel:
  - 2 pedidos Novos
  - 2 pedidos Entregues (com data de conclusão)
  - Demais distribuídos entre outros estados

### Cupons Disponíveis (em memória)
| Código | Desconto | Período de Validade |
|---|---|---|
| `JUNHO10` | 10% | 01/06/2026 – 30/06/2026 |
| `DESC10` | 10% | 25/04/2026 – 27/04/2026 |
| `DESC20` | 20% | 01/05/2026 – 05/05/2026 |
| `DESC30` | 30% | 24/04/2026 |
| `BLACK50` | 50% | 28/04/2026 |
| `DIAPAI12` | 12% | 09/05/2026 – 10/05/2026 |
| `DIAMAE12` | 12% | 10/05/2026 – 12/05/2026 |
| `NATAL10` | 10% | 20/04/2026 – 26/04/2026 |
| `FESTA15` | 15% | 30/04/2026 18h – 01/05/2026 6h |

> **Nota:** Apenas cupons dentro do período de validade na data de operação são aplicáveis.

---

##  Banco de Dados

O SQLite (`delivery.db`) é criado automaticamente e contém as seguintes tabelas:

| Tabela | Descrição |
|---|---|
| `usuarios` | Usuários com perfil e situação |
| `clientes` | Clientes com nome e CPF único |
| `enderecos` | Endereços de entrega (FK → clientes) |
| `produtos` | Catálogo com código único, preço e estoque |
| `estoque_movimentacoes` | Histórico de movimentações com tipo, motivo/NF |
| `pedidos` | Pedidos com estado, valores e datas |
| `pedido_itens` | Itens do pedido (FK → pedidos, produtos) |
| `pagamentos` | Tentativas de pagamento com resultado e forma |

### Configurações do SQLite
- **WAL** (Write-Ahead Log) — permite leituras e escritas concorrentes
- **busy_timeout = 5000ms** — evita erros de "database locked"
- **foreign_keys = ON** — integridade referencial ativa

---

##  Autores

SUELEN SALAROLLI BISI , MARIA CLARA GUELER FEITANI e HENRIQUE QUEIROZ TEIXEIRA

---

## 📄 Referências

- CUCUMBER. *Gherkin reference*. 2026. Disponível em: https://cucumber.io/docs/gherkin/reference/
- CARTER, J.; GARDNER, W. B. *BHive: behavior-driven development meets B-method*. Springer, 2016.
- SILVA, Marcus et al. *Automated test generation using LLM based on BDD: a comparative study*. 2025.
