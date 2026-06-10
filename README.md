# Pós-Vendas Seminovos — App Android

Aplicativo Android (Java) para o **pós-venda de uma concessionária de seminovos**.
Permite cadastrar clientes e veículos, registrar ocorrências (problemas) e agendar o
diagnóstico do mecânico, além de gerar um relatório em arquivo `.txt`.

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
              data_retirada, garantia_adicional, foto_caminho)
ocorrencias  (id, veiculo_id→veiculos, descricao, data_registro,
              status, data_diagnostico)
```

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
Apenas navegação: 5 botões (Clientes, Veículos, Ocorrências, Agenda, Relatório),
cada um com ícone Material, abrindo a Activity correspondente por `Intent`.

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

### OcorrenciasActivity
- Spinner de veículo + descrição (multiline) + **Registrar**.
- `INSERT` grava a **data atual automaticamente** e status **ABERTA**.
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
- **Carregar Relatório** → lê o arquivo com **`FileInputStream`** e mostra num
  `TextView` dentro de `ScrollView`. Se o arquivo não existir, avisa o usuário.

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

## 8. Roteiro sugerido para a apresentação

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

## 9. Critérios de aceite atendidos

- ✔ Compila e roda em API 24+ sem ajuste manual (`BUILD SUCCESSFUL`).
- ✔ Login `admin/admin123` funciona no primeiro boot.
- ✔ CRUD completo em clientes e veículos (com confirmação de exclusão).
- ✔ Fluxo de permissão da câmera visível (Toast de concedida/negada).
- ✔ Foto salva em arquivo + caminho no SQLite; miniatura recarrega ao reabrir.
- ✔ Fluxo completo sem crash: cliente → veículo com foto → ocorrência →
  agendamento → relatório `.txt` gerado e lido.
- ✔ Nenhum campo aceita submissão vazia.
- ✔ Dados persistem após fechar e reabrir o app.
