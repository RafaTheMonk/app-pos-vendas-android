package br.com.posvendas.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

import br.com.posvendas.DatabaseHelper;
import br.com.posvendas.R;
import br.com.posvendas.adapter.ClienteAdapter;
import br.com.posvendas.model.Cliente;

/**
 * Tela de Clientes: CRUD completo.
 * - Salvar  -> INSERT
 * - Tocar no item da lista -> carrega nos campos para edição
 * - Atualizar -> UPDATE pelo id
 * - Lixeira no item -> AlertDialog de confirmação -> DELETE
 * A lista é recarregada do banco a cada operação para refletir o estado real.
 */
public class ClientesActivity extends AppCompatActivity
        implements ClienteAdapter.OnClienteAcaoListener {

    private EditText etNome;
    private EditText etTelefone;
    private EditText etEmail;
    private Button btnSalvar;
    private Button btnAtualizar;
    private Button btnLimpar;
    private RecyclerView rvClientes;

    private DatabaseHelper bd;
    private ClienteAdapter adapter;
    private List<Cliente> clientes;

    // Guarda o id do cliente que está sendo editado (0 = nenhum, modo de inserção).
    private int idEmEdicao = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clientes);

        // Toolbar com seta de voltar: encerra a tela e retorna ao menu.
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        bd = new DatabaseHelper(this);

        etNome = findViewById(R.id.etNome);
        etTelefone = findViewById(R.id.etTelefone);
        etEmail = findViewById(R.id.etEmail);
        btnSalvar = findViewById(R.id.btnSalvarCliente);
        btnAtualizar = findViewById(R.id.btnAtualizarCliente);
        btnLimpar = findViewById(R.id.btnLimparCliente);
        rvClientes = findViewById(R.id.rvClientes);

        // Configura a lista (RecyclerView com layout vertical).
        rvClientes.setLayoutManager(new LinearLayoutManager(this));
        clientes = bd.listarClientes();
        adapter = new ClienteAdapter(clientes, this);
        rvClientes.setAdapter(adapter);

        // No começo só dá para Salvar (inserir); Atualizar fica desabilitado.
        habilitarModoInsercao();

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarCliente();
            }
        });

        btnAtualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                atualizarCliente();
            }
        });

        btnLimpar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                limparFormulario();
            }
        });
    }

    /** INSERT: valida os campos e grava um novo cliente. */
    private void salvarCliente() {
        if (!camposValidos()) {
            return;
        }

        Cliente cliente = new Cliente();
        cliente.setNome(etNome.getText().toString().trim());
        cliente.setTelefone(etTelefone.getText().toString().trim());
        cliente.setEmail(etEmail.getText().toString().trim());

        long id = bd.inserirCliente(cliente);
        if (id != -1) {
            Toast.makeText(this, "Cliente salvo com sucesso", Toast.LENGTH_SHORT).show();
            limparFormulario();
            recarregarLista();
        } else {
            Toast.makeText(this, "Erro ao salvar cliente", Toast.LENGTH_SHORT).show();
        }
    }

    /** UPDATE: grava as alterações do cliente que está em edição. */
    private void atualizarCliente() {
        if (idEmEdicao == 0) {
            return; // segurança: só atualiza se algo foi selecionado
        }
        if (!camposValidos()) {
            return;
        }

        Cliente cliente = new Cliente();
        cliente.setId(idEmEdicao);
        cliente.setNome(etNome.getText().toString().trim());
        cliente.setTelefone(etTelefone.getText().toString().trim());
        cliente.setEmail(etEmail.getText().toString().trim());

        int linhas = bd.atualizarCliente(cliente);
        if (linhas > 0) {
            Toast.makeText(this, "Cliente atualizado", Toast.LENGTH_SHORT).show();
            limparFormulario();
            recarregarLista();
        } else {
            Toast.makeText(this, "Erro ao atualizar", Toast.LENGTH_SHORT).show();
        }
    }

    /** Validação dos campos: vazios e formato de e-mail. */
    private boolean camposValidos() {
        String nome = etNome.getText().toString().trim();
        String telefone = etTelefone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(nome)) {
            etNome.setError("Informe o nome");
            return false;
        }
        if (TextUtils.isEmpty(telefone)) {
            etTelefone.setError("Informe o telefone");
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Informe o e-mail");
            return false;
        }
        // Patterns.EMAIL_ADDRESS é um validador pronto do Android para e-mail.
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("E-mail inválido");
            return false;
        }
        return true;
    }

    /** Toque no item da lista: carrega os dados no formulário para editar. */
    @Override
    public void onEditarCliente(Cliente cliente) {
        idEmEdicao = cliente.getId();
        etNome.setText(cliente.getNome());
        etTelefone.setText(cliente.getTelefone());
        etEmail.setText(cliente.getEmail());
        habilitarModoEdicao();
    }

    /** Lixeira no item: pede confirmação antes de excluir (AlertDialog). */
    @Override
    public void onExcluirCliente(final Cliente cliente) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir cliente")
                .setMessage("Deseja realmente excluir \"" + cliente.getNome() + "\"?")
                .setPositiveButton("Excluir", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        bd.excluirCliente(cliente.getId());
                        Toast.makeText(ClientesActivity.this, "Cliente excluído", Toast.LENGTH_SHORT).show();
                        limparFormulario();
                        recarregarLista();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /** Recarrega a lista a partir do banco e avisa o adapter para redesenhar. */
    private void recarregarLista() {
        clientes.clear();
        clientes.addAll(bd.listarClientes());
        adapter.notifyDataSetChanged();
    }

    /** Limpa o formulário e volta para o modo de inserção. */
    private void limparFormulario() {
        idEmEdicao = 0;
        etNome.setText("");
        etTelefone.setText("");
        etEmail.setText("");
        etNome.setError(null);
        etTelefone.setError(null);
        etEmail.setError(null);
        habilitarModoInsercao();
    }

    /** Modo inserção: habilita Salvar, desabilita Atualizar. */
    private void habilitarModoInsercao() {
        btnSalvar.setEnabled(true);
        btnAtualizar.setEnabled(false);
    }

    /** Modo edição: desabilita Salvar, habilita Atualizar. */
    private void habilitarModoEdicao() {
        btnSalvar.setEnabled(false);
        btnAtualizar.setEnabled(true);
    }
}
