package br.com.posvendas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import br.com.posvendas.model.Cliente;
import br.com.posvendas.model.Ocorrencia;
import br.com.posvendas.model.Usuario;
import br.com.posvendas.model.Veiculo;

/**
 * Classe central de banco de dados.
 *
 * Toda a comunicação com o SQLite passa por aqui (sem Room, sem ORM).
 * - Os comandos de CRIAÇÃO das tabelas ficam em onCreate().
 * - As ESCRITAS (INSERT/UPDATE/DELETE) usam ContentValues + os métodos
 *   insert()/update()/delete() do SQLiteDatabase.
 * - As LEITURAS (SELECT) usam rawQuery(), inclusive os SELECT com JOIN.
 * Mantemos esse padrão em todos os métodos para ficar consistente.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Nome do arquivo de banco e versão (incrementar a versão dispara onUpgrade).
    private static final String NOME_BANCO = "posvendas.db";
    private static final int VERSAO_BANCO = 2;

    // Nomes das tabelas centralizados em constantes (evita erro de digitação).
    public static final String TABELA_USUARIOS = "usuarios";
    public static final String TABELA_CLIENTES = "clientes";
    public static final String TABELA_VEICULOS = "veiculos";
    public static final String TABELA_OCORRENCIAS = "ocorrencias";

    public DatabaseHelper(Context context) {
        // super() apenas registra o nome/versão; o banco só é criado de fato
        // na primeira vez que pedimos getReadableDatabase()/getWritableDatabase().
        super(context, NOME_BANCO, null, VERSAO_BANCO);
    }

    /**
     * Executado UMA única vez, quando o banco é criado pela primeira vez.
     * Aqui criamos todas as tabelas com SQL explícito e inserimos o usuário seed.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Tabela de usuários do sistema (login da aplicação).
        db.execSQL("CREATE TABLE " + TABELA_USUARIOS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "login TEXT NOT NULL UNIQUE, " +
                "senha TEXT NOT NULL, " +
                "perfil TEXT NOT NULL" +
                ");");

        // Tabela de clientes da concessionária.
        db.execSQL("CREATE TABLE " + TABELA_CLIENTES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL, " +
                "telefone TEXT NOT NULL, " +
                "email TEXT NOT NULL" +
                ");");

        // Tabela de veículos. cliente_id é chave estrangeira para clientes(id).
        db.execSQL("CREATE TABLE " + TABELA_VEICULOS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "cliente_id INTEGER NOT NULL, " +
                "modelo TEXT NOT NULL, " +
                "ano INTEGER NOT NULL, " +
                "chassi TEXT NOT NULL UNIQUE, " +
                "placa TEXT NOT NULL UNIQUE, " +
                "data_retirada TEXT NOT NULL, " +
                "garantia_adicional TEXT, " +
                "foto_caminho TEXT, " +
                "valor_cobranca REAL NOT NULL DEFAULT 0, " +
                "FOREIGN KEY (cliente_id) REFERENCES " + TABELA_CLIENTES + "(id)" +
                ");");

        // Tabela de ocorrências (problemas relatados). veiculo_id -> veiculos(id).
        db.execSQL("CREATE TABLE " + TABELA_OCORRENCIAS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "veiculo_id INTEGER NOT NULL, " +
                "descricao TEXT NOT NULL, " +
                "data_registro TEXT NOT NULL, " +
                "status TEXT NOT NULL DEFAULT 'ABERTA', " +
                "data_diagnostico TEXT, " +
                "valor_diagnostico REAL NOT NULL DEFAULT 0, " +
                "FOREIGN KEY (veiculo_id) REFERENCES " + TABELA_VEICULOS + "(id)" +
                ");");

        // Usuário seed: garante que dá para logar no primeiro boot do app.
        db.execSQL("INSERT INTO " + TABELA_USUARIOS + " (login, senha, perfil) " +
                "VALUES ('admin', 'admin123', 'ADMIN');");
    }

    /**
     * Chamado quando a VERSAO_BANCO aumenta. Como é projeto acadêmico (versão 1),
     * a estratégia simples é apagar tudo e recriar.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_OCORRENCIAS + ";");
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_VEICULOS + ";");
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_CLIENTES + ";");
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_USUARIOS + ";");
        onCreate(db);
    }

    // ====================================================================
    // USUÁRIOS
    // ====================================================================

    /**
     * Valida o login: faz um SELECT procurando um usuário com o login E a senha
     * informados. Se achar, devolve o Usuario; se não achar, devolve null.
     * Usamos "?" (argumentos vinculados) para evitar SQL Injection.
     */
    public Usuario validarLogin(String login, String senha) {
        SQLiteDatabase db = getReadableDatabase();
        Usuario usuario = null;

        Cursor cursor = db.rawQuery(
                "SELECT id, login, senha, perfil FROM " + TABELA_USUARIOS +
                        " WHERE login = ? AND senha = ?",
                new String[]{login, senha});

        if (cursor.moveToFirst()) {
            usuario = new Usuario(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3));
        }
        cursor.close();
        return usuario;
    }

    // ====================================================================
    // CLIENTES  (CRUD completo)
    // ====================================================================

    /** INSERT de um cliente. Devolve o id gerado (ou -1 em caso de erro). */
    public long inserirCliente(Cliente cliente) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("nome", cliente.getNome());
        valores.put("telefone", cliente.getTelefone());
        valores.put("email", cliente.getEmail());
        return db.insert(TABELA_CLIENTES, null, valores);
    }

    /** UPDATE de um cliente pelo id. Devolve a quantidade de linhas afetadas. */
    public int atualizarCliente(Cliente cliente) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("nome", cliente.getNome());
        valores.put("telefone", cliente.getTelefone());
        valores.put("email", cliente.getEmail());
        return db.update(TABELA_CLIENTES, valores, "id = ?",
                new String[]{String.valueOf(cliente.getId())});
    }

    /** DELETE de um cliente pelo id. Devolve a quantidade de linhas afetadas. */
    public int excluirCliente(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABELA_CLIENTES, "id = ?",
                new String[]{String.valueOf(id)});
    }

    /** SELECT de todos os clientes, em ordem alfabética. */
    public List<Cliente> listarClientes() {
        List<Cliente> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id, nome, telefone, email FROM " + TABELA_CLIENTES +
                        " ORDER BY nome", null);

        while (cursor.moveToNext()) {
            Cliente c = new Cliente(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3));
            lista.add(c);
        }
        cursor.close();
        return lista;
    }

    // ====================================================================
    // VEÍCULOS  (CRUD completo + foto)
    // ====================================================================

    /**
     * INSERT de um veículo. Como chassi e placa são UNIQUE, se já existir um
     * igual o insert() devolve -1 (e a Activity mostra uma mensagem amigável).
     */
    public long inserirVeiculo(Veiculo veiculo) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = montarValoresVeiculo(veiculo);
        try {
            // insertOrThrow lança exceção se violar o UNIQUE de chassi/placa.
            return db.insertOrThrow(TABELA_VEICULOS, null, valores);
        } catch (SQLiteConstraintException e) {
            // Chassi ou placa já cadastrados: devolve -1 e a Activity avisa o usuário.
            return -1;
        }
    }

    /** UPDATE de um veículo pelo id. Devolve -1 se violar o UNIQUE (chassi/placa). */
    public int atualizarVeiculo(Veiculo veiculo) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = montarValoresVeiculo(veiculo);
        try {
            return db.update(TABELA_VEICULOS, valores, "id = ?",
                    new String[]{String.valueOf(veiculo.getId())});
        } catch (SQLiteConstraintException e) {
            return -1;
        }
    }

    /** Monta o ContentValues do veículo (reaproveitado no insert e no update). */
    private ContentValues montarValoresVeiculo(Veiculo veiculo) {
        ContentValues valores = new ContentValues();
        valores.put("cliente_id", veiculo.getClienteId());
        valores.put("modelo", veiculo.getModelo());
        valores.put("ano", veiculo.getAno());
        valores.put("chassi", veiculo.getChassi());
        valores.put("placa", veiculo.getPlaca());
        valores.put("data_retirada", veiculo.getDataRetirada());
        valores.put("garantia_adicional", veiculo.getGarantiaAdicional());
        valores.put("foto_caminho", veiculo.getFotoCaminho());
        valores.put("valor_cobranca", veiculo.getValorCobranca());
        return valores;
    }

    /** DELETE de um veículo pelo id. */
    public int excluirVeiculo(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABELA_VEICULOS, "id = ?",
                new String[]{String.valueOf(id)});
    }

    /**
     * SELECT de todos os veículos com JOIN em clientes para trazer o nome do dono.
     * O nome vai no campo de apoio clienteNome (que não é coluna da tabela veiculos).
     */
    public List<Veiculo> listarVeiculos() {
        List<Veiculo> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT v.id, v.cliente_id, v.modelo, v.ano, v.chassi, v.placa, " +
                        "v.data_retirada, v.garantia_adicional, v.foto_caminho, " +
                        "v.valor_cobranca, c.nome " +
                        "FROM " + TABELA_VEICULOS + " v " +
                        "INNER JOIN " + TABELA_CLIENTES + " c ON v.cliente_id = c.id " +
                        "ORDER BY v.modelo", null);

        while (cursor.moveToNext()) {
            Veiculo v = new Veiculo();
            v.setId(cursor.getInt(0));
            v.setClienteId(cursor.getInt(1));
            v.setModelo(cursor.getString(2));
            v.setAno(cursor.getInt(3));
            v.setChassi(cursor.getString(4));
            v.setPlaca(cursor.getString(5));
            v.setDataRetirada(cursor.getString(6));
            v.setGarantiaAdicional(cursor.getString(7));
            v.setFotoCaminho(cursor.getString(8));
            v.setValorCobranca(cursor.getDouble(9));
            v.setClienteNome(cursor.getString(10));
            lista.add(v);
        }
        cursor.close();
        return lista;
    }

    // ====================================================================
    // OCORRÊNCIAS
    // ====================================================================

    /** INSERT de uma ocorrência (status e data de registro já vêm preenchidos). */
    public long inserirOcorrencia(Ocorrencia ocorrencia) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("veiculo_id", ocorrencia.getVeiculoId());
        valores.put("descricao", ocorrencia.getDescricao());
        valores.put("data_registro", ocorrencia.getDataRegistro());
        valores.put("status", ocorrencia.getStatus());
        valores.put("data_diagnostico", ocorrencia.getDataDiagnostico());
        valores.put("valor_diagnostico", ocorrencia.getValorDiagnostico());
        return db.insert(TABELA_OCORRENCIAS, null, valores);
    }

    /** DELETE de uma ocorrência pelo id. */
    public int excluirOcorrencia(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABELA_OCORRENCIAS, "id = ?",
                new String[]{String.valueOf(id)});
    }

    /**
     * Agenda o diagnóstico: UPDATE que grava a data e muda o status para AGENDADA.
     */
    public int agendarDiagnostico(int ocorrenciaId, String dataDiagnostico) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("data_diagnostico", dataDiagnostico);
        valores.put("status", Ocorrencia.STATUS_AGENDADA);
        return db.update(TABELA_OCORRENCIAS, valores, "id = ?",
                new String[]{String.valueOf(ocorrenciaId)});
    }

    /**
     * SELECT de TODAS as ocorrências com JOIN nas 3 tabelas (ocorrencia ->
     * veiculo -> cliente), trazendo nome do cliente, modelo e placa para exibir.
     * Usado na tela de Ocorrências e na geração do Relatório.
     */
    public List<Ocorrencia> listarOcorrencias() {
        return consultarOcorrencias(null);
    }

    /**
     * Igual ao anterior, mas filtrando por status (ex.: só as ABERTAS na Agenda).
     * Se status for null, traz todas.
     */
    public List<Ocorrencia> listarOcorrenciasPorStatus(String status) {
        return consultarOcorrencias(status);
    }

    /** Método interno que monta o SELECT com JOIN, com ou sem filtro de status. */
    private List<Ocorrencia> consultarOcorrencias(String status) {
        List<Ocorrencia> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT o.id, o.veiculo_id, o.descricao, o.data_registro, " +
                "o.status, o.data_diagnostico, o.valor_diagnostico, " +
                "c.nome, v.modelo, v.placa " +
                "FROM " + TABELA_OCORRENCIAS + " o " +
                "INNER JOIN " + TABELA_VEICULOS + " v ON o.veiculo_id = v.id " +
                "INNER JOIN " + TABELA_CLIENTES + " c ON v.cliente_id = c.id ";

        Cursor cursor;
        if (status == null) {
            sql += "ORDER BY o.id DESC";
            cursor = db.rawQuery(sql, null);
        } else {
            // Filtro por status usando argumento vinculado.
            sql += "WHERE o.status = ? ORDER BY o.id DESC";
            cursor = db.rawQuery(sql, new String[]{status});
        }

        while (cursor.moveToNext()) {
            Ocorrencia o = new Ocorrencia();
            o.setId(cursor.getInt(0));
            o.setVeiculoId(cursor.getInt(1));
            o.setDescricao(cursor.getString(2));
            o.setDataRegistro(cursor.getString(3));
            o.setStatus(cursor.getString(4));
            o.setDataDiagnostico(cursor.getString(5));
            o.setValorDiagnostico(cursor.getDouble(6));
            o.setClienteNome(cursor.getString(7));
            o.setVeiculoModelo(cursor.getString(8));
            o.setPlaca(cursor.getString(9));
            lista.add(o);
        }
        cursor.close();
        return lista;
    }
}
