# Pós-Vendas Seminovos — App Android

Aplicativo Android (Java) para o **pós-venda de uma concessionária de seminovos**.
Permite cadastrar clientes e veículos, registrar ocorrências (problemas) e agendar o
diagnóstico do mecânico, além de gerar um relatório em arquivo `.txt`.

Conta ainda com um **Painel** de resumo, **botão de voltar** em todas as telas
internas e o controle de **garantia/cobrança** (veículo fora da garantia gera
cobrança fixa; toda ocorrência gera um valor fixo de diagnóstico do mecânico).

Projeto acadêmico construído usando **apenas as APIs vistas em aula**: banco SQLite
puro com `SQLiteOpenHelper`, permissões em tempo de execução, câmera por `Intent`,
`RecyclerView` com adapters próprios e Material Design.

---

## 1. Tecnologias usadas

| Item | Tecnologia |
|------|-----------|
| Linguagem | Java |
| Banco de dados | SQLite puro via `SQLiteOpenHelper` (sem Room/ORM) |
| Listas | `RecyclerView` + Adapter próprio |
| Câmera | `Intent(MediaStore.ACTION_IMAGE_CAPTURE)` |
| Permissões | `ContextCompat` / `ActivityCompat` / `onRequestPermissionsResult` |
| Arquivos | `FileOutputStream` / `FileInputStream` em `getFilesDir()` |
| UI | XML + Material Components (Material 3) |
| Min SDK / Target SDK | 24 / 34 |

Arquitetura **simples e direta**: cada `Activity` chama diretamente a classe
`DatabaseHelper`. Sem MVVM, ViewModel, LiveData ou Repository.

---

## 2. Como executar

1. Abrir a pasta raiz do projeto no **Android Studio** (a pasta que contém
   `settings.gradle.kts`).
2. Aguardar o **Gradle Sync**.
3. Conectar um celular (com **Depuração USB** ligada) ou iniciar um emulador API 24+.
4. Clicar em **Run ▶**.
5. Login inicial:

   - **Usuário:** `admin`
   - **Senha:** `admin123`

O usuário `admin` é criado automaticamente na primeira execução (seed do banco).

---

## 3. Estrutura do projeto

```
br.com.posvendas
├── DatabaseHelper.java        → TODO o SQL fica centralizado aqui
├── model/                     → POJOs (objetos simples que carregam os dados)
│   ├── Usuario.java
│   ├── Cliente.java
│   ├── Veiculo.java
│   └── Ocorrencia.java
├── adapter/                   → ligam as listas (RecyclerView) aos dados
│   ├── ClienteAdapter.java
│   ├── VeiculoAdapter.java
│   ├── OcorrenciaAdapter.java
│   └── AgendaAdapter.java
└── activities/                → as telas do app
    ├── LoginActivity.java
    ├── MenuActivity.java
    ├── DashboardActivity.java   → Painel de resumo (somente leitura)
    ├── ClientesActivity.java
    ├── VeiculosActivity.java
    ├── OcorrenciasActivity.java
    ├── AgendaActivity.java
    └── RelatorioActivity.java
```

**Por que essa divisão?**
- `model` = só dados (nome, telefone...). Não conhece banco nem tela.
- `adapter` = pega uma lista de `model` e desenha cada linha na tela.
- `activities` = a tela em si; reage aos toques e chama o `DatabaseHelper`.
- `DatabaseHelper` = único ponto que conversa com o SQLite.

---

## 4. Banco de dados (`posvendas.db`)

Criado em `DatabaseHelper.onCreate()` com SQL explícito. São 4 tabelas:

```
usuarios     (id, login, senha, perfil)
clientes     (id, nome, telefone, email)
veiculos     (id, cliente_id→clientes, modelo, ano, chassi, placa,
              data_retirada, garantia_adicional, foto_caminho, valor_cobranca)
ocorrencias  (id, veiculo_id→veiculos, descricao, data_registro,
              status, data_diagnostico, valor_diagnostico)
```

> **Versão do banco = 2.** As colunas `valor_cobranca` (veículos) e
> `valor_diagnostico` (ocorrências) foram adicionadas na versão 2. Ao subir a
> versão, `onUpgrade()` recria as tabelas (estratégia simples do projeto), então
> ao instalar por cima o banco antigo é refeito.

**Relacionamentos (chaves estrangeiras):**

```
cliente  1 ───< N  veiculo  1 ───< N  ocorrencia
```

Um cliente tem vários veículos; um veículo tem várias ocorrências.
As datas são guardadas como TEXT no formato `dd/MM/yyyy`.

**Padrão de acesso ao banco (importante na defesa):**
- **Escritas** (INSERT / UPDATE / DELETE) → `ContentValues` + `insert()`, `update()`, `delete()`.
- **Leituras** (SELECT, incluindo os JOIN) → `rawQuery()`.
- Argumentos sempre passados com `?` para evitar SQL Injection.

---

## 5. O que cada tela faz

### LoginActivity
Campos usuário/senha. Faz `SELECT` em `usuarios` validando login **e** senha.
Valida campos vazios antes de consultar. Acerto → vai para o Menu; erro → Toast
"Usuário ou senha inválidos".

### MenuActivity
Apenas navegação: 6 botões (**Painel**, Clientes, Veículos, Ocorrências, Agenda,
Relatório), cada um com ícone Material, abrindo a Activity correspondente por `Intent`.

### DashboardActivity (Painel) — tela de resumo
Tela **somente leitura** com contadores vindos do banco: total de ocorrências,
nº de clientes, nº de veículos, ocorrências **abertas** (laranja) e **agendadas**
(verde). Os números são recalculados em `onResume()`, então refletem sempre o
estado atual. Botão **Atualizar** recarrega manualmente.

> Todas as telas internas têm um **botão de voltar** (seta na `MaterialToolbar`)
> que encerra a tela e retorna ao menu.

### ClientesActivity — CRUD completo
- **Salvar** → `INSERT`.
- **Tocar no item** → carrega os dados no formulário para editar.
- **Atualizar** → `UPDATE` pelo `id`.
- **Lixeira** → `AlertDialog` de confirmação → `DELETE`.
- Valida campos vazios e **formato de e-mail** (`Patterns.EMAIL_ADDRESS`).
- Lista em `RecyclerView` recarregada do banco a cada operação.

### VeiculosActivity — CRUD + foto (tela mais completa)
- Formulário com **Spinner de cliente**, modelo, ano, chassi, placa,
  **data de retirada (DatePickerDialog)** e garantia.
- **Foto pela câmera** (detalhado no item 6).
- Validações: campos vazios, **ano numérico**, **chassi com 17 caracteres** e
  **placa/chassi duplicados** (tratamento do `UNIQUE` — ver item 7).
- Lista mostra a **miniatura da foto** carregada do caminho salvo no banco.
- **Garantia/cobrança**: ao salvar, o app calcula se o veículo está fora da
  garantia (ver item 8). Se estiver, grava uma **cobrança fixa de R$ 300,00**
  (`valor_cobranca`) e avisa no Toast. A lista mostra *"Na garantia"* (verde) ou
  *"Fora da garantia · R$ 300,00"* (laranja).

### OcorrenciasActivity
- Spinner de veículo + descrição (multiline) + **Registrar**.
- `INSERT` grava a **data atual automaticamente** e status **ABERTA**.
- Gera automaticamente um **valor fixo de diagnóstico do mecânico de R$ 150,00**
  (`valor_diagnostico`), exibido na lista e no relatório.
- Lista mostra o status **colorido** (ABERTA = laranja, AGENDADA = verde).
- Excluir com `AlertDialog`.

### AgendaActivity (visão do mecânico)
- Duas listas vindas de um **`SELECT` com JOIN nas 3 tabelas**
  (ocorrência → veículo → cliente):
  - **Aguardando diagnóstico** (abertas) com botão **Agendar**.
  - **Diagnósticos agendados** com a data marcada.
- Agendar → `DatePickerDialog` → `UPDATE` gravando `data_diagnostico` e
  mudando o status para **AGENDADA**.

### RelatorioActivity
- **Gerar Relatório** → percorre as ocorrências (`SELECT`) e grava o arquivo
  `relatorio.txt` em `getFilesDir()` usando **`FileOutputStream`**.
- **Carregar Relatório** → lê o arquivo com **`FileInputStream`** (via
  `InputStreamReader` em **UTF-8**) e mostra num `TextView` dentro de `ScrollView`.
  Se o arquivo não existir, avisa o usuário.
- Cada linha inclui o **valor do diagnóstico**. A escrita/leitura usa **UTF-8
  explícito**, então acentos (Relatório, Veículo, Diagnóstico) saem corretos.

---

## 6. Fluxo da câmera + permissão (passo a passo)

Tudo em `VeiculosActivity`. É o ponto mais cobrado:

1. Ao tocar em **"Tirar foto"**, chamamos `ContextCompat.checkSelfPermission()`
   para ver se a permissão de **CAMERA** já foi concedida.
2. Se **não**, pedimos com `ActivityCompat.requestPermissions()`.
3. A resposta chega em **`onRequestPermissionsResult()`**, que mostra um Toast
   ("concedida" ou "negada"). Se concedida, abre a câmera.
4. A câmera é aberta com `Intent(MediaStore.ACTION_IMAGE_CAPTURE)`.
5. O resultado volta no `ActivityResultLauncher`. A foto (miniatura) é
   **gravada em arquivo** dentro de `getFilesDir()` com `FileOutputStream`.
6. O **caminho** do arquivo é salvo na coluna `foto_caminho` e a imagem aparece
   no `ImageView`. Ao reabrir o app, a miniatura é recarregada desse caminho.

> Como gravamos em `getFilesDir()` (área privada do app), **não** é preciso
> permissão de armazenamento — mas o fluxo de pedir permissão (CAMERA) existe e
> é demonstrável.

---

## 7. Validações e tratamento de erros

- **Campos vazios**: bloqueados em todas as telas antes de qualquer SQL.
- **E-mail**: validado com `Patterns.EMAIL_ADDRESS`.
- **Ano**: precisa ser numérico.
- **Chassi**: exatamente 17 caracteres.
- **Placa/Chassi duplicados**: as colunas são `UNIQUE`. No `INSERT`/`UPDATE`
  capturamos `SQLiteConstraintException` e devolvemos `-1`; a tela mostra a
  mensagem amigável *"Chassi ou placa já cadastrados"* em vez de quebrar o app.
- **Relatório inexistente**: ao carregar, verificamos `arquivo.exists()` antes de ler.

---

## 8. Garantia e cobrança

Regra usada para decidir se um veículo está **fora da garantia**:

- Garantia padrão de **3 meses** (`Veiculo.GARANTIA_PADRAO_MESES`) a partir da
  **data de retirada**.
- O campo **garantia adicional** conta como **meses extras** (o app extrai só os
  dígitos, então aceita "6" ou "6 meses"; texto inválido/vazio = 0 extra).
- Se a data de hoje for **posterior** ao fim do prazo (retirada + 3 + extras), o
  veículo está **fora da garantia**.

Valores fixos aplicados:

| Evento | Valor | Onde |
|--------|-------|------|
| Veículo cadastrado **fora da garantia** | **R$ 300,00** | `veiculos.valor_cobranca` |
| **Diagnóstico** do mecânico (toda ocorrência) | **R$ 150,00** | `ocorrencias.valor_diagnostico` |

A lógica de garantia fica em `Veiculo.estaForaDaGarantia(...)` (testável, sem
depender de tela). A formatação em reais usa `Locale("pt","BR")` → `R$ 150,00`.

---

## 9. Roteiro sugerido para a apresentação

1. Mostrar o **login** com `admin / admin123` e explicar o `SELECT` de validação.
2. Cadastrar um **cliente** (mostrar INSERT, depois editar = UPDATE, depois a
   confirmação de exclusão com AlertDialog).
3. Cadastrar um **veículo** desse cliente:
   - escolher o cliente no Spinner,
   - escolher a data no DatePicker,
   - **tirar a foto** (mostrar o pedido de permissão da câmera),
   - tentar salvar uma **placa repetida** para mostrar o tratamento do UNIQUE.
4. Registrar uma **ocorrência** para o veículo (status ABERTA, em laranja).
5. Ir na **Agenda**, agendar o diagnóstico (status vira AGENDADA, em verde) —
   explicar que aqui há um **JOIN das 3 tabelas**.
6. Gerar e carregar o **relatório .txt** (FileOutputStream / FileInputStream).
7. Fechar e reabrir o app para provar que **os dados persistem** e a **foto volta**.

---

## 10. Critérios de aceite atendidos

- ✔ Compila e roda em API 24+ sem ajuste manual (`BUILD SUCCESSFUL`).
- ✔ Login `admin/admin123` funciona no primeiro boot.
- ✔ CRUD completo em clientes e veículos (com confirmação de exclusão).
- ✔ Fluxo de permissão da câmera visível (Toast de concedida/negada).
- ✔ Foto salva em arquivo + caminho no SQLite; miniatura recarrega ao reabrir.
- ✔ Fluxo completo sem crash: cliente → veículo com foto → ocorrência →
  agendamento → relatório `.txt` gerado e lido.
- ✔ Nenhum campo aceita submissão vazia.
- ✔ Dados persistem após fechar e reabrir o app.
- ✔ Painel mostra contadores corretos (clientes, veículos, abertas, agendadas).
- ✔ Botão de voltar em todas as telas internas.
- ✔ Veículo fora da garantia gera cobrança de R$ 300,00; ocorrência gera
  diagnóstico de R$ 150,00.
- ✔ Relatório `.txt` com acentos corretos (UTF-8).
- ✔ Ícone do app personalizado (carro sobre fundo azul da marca).
