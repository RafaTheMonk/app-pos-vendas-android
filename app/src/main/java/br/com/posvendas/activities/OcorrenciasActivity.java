package br.com.posvendas.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.posvendas.DatabaseHelper;
import br.com.posvendas.R;
import br.com.posvendas.adapter.OcorrenciaAdapter;
import br.com.posvendas.model.Ocorrencia;
import br.com.posvendas.model.Veiculo;

/**
 * Tela de Ocorrências: registra um problema relatado para um veículo.
 * O INSERT já grava a data atual (data_registro) e o status inicial ABERTA.
 * A lista mostra o status com cor (ABERTA laranja / AGENDADA verde) e permite excluir.
 */
public class OcorrenciasActivity extends AppCompatActivity
        implements OcorrenciaAdapter.OnOcorrenciaAcaoListener {

    private Spinner spinnerVeiculo;
    private EditText etDescricao;
    private Button btnRegistrar;
    private RecyclerView rvOcorrencias;

    private DatabaseHelper bd;
    private OcorrenciaAdapter adapter;
    private List<Ocorrencia> ocorrencias;
    private List<Veiculo> veiculos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocorrencias);

        // Toolbar com seta de voltar: encerra a tela e retorna ao menu.
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        bd = new DatabaseHelper(this);

        spinnerVeiculo = findViewById(R.id.spinnerVeiculo);
        etDescricao = findViewById(R.id.etDescricao);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        rvOcorrencias = findViewById(R.id.rvOcorrencias);

        carregarVeiculosNoSpinner();

        rvOcorrencias.setLayoutManager(new LinearLayoutManager(this));
        ocorrencias = bd.listarOcorrencias();
        adapter = new OcorrenciaAdapter(ocorrencias, this);
        rvOcorrencias.setAdapter(adapter);

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarOcorrencia();
            }
        });
    }

    /** Carrega os veículos disponíveis no Spinner. */
    private void carregarVeiculosNoSpinner() {
        veiculos = bd.listarVeiculos();
        ArrayAdapter<Veiculo> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, veiculos);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVeiculo.setAdapter(spinnerAdapter);
    }

    /** Valida e grava a ocorrência com data atual e status ABERTA. */
    private void registrarOcorrencia() {
        // Precisa de um veículo selecionado.
        if (veiculos == null || veiculos.isEmpty() || spinnerVeiculo.getSelectedItem() == null) {
            Toast.makeText(this, "Cadastre um veículo antes de registrar ocorrências",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String descricao = etDescricao.getText().toString().trim();
        if (TextUtils.isEmpty(descricao)) {
            etDescricao.setError("Descreva o problema");
            return;
        }

        Veiculo veiculoSelecionado = (Veiculo) spinnerVeiculo.getSelectedItem();

        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setVeiculoId(veiculoSelecionado.getId());
        ocorrencia.setDescricao(descricao);
        ocorrencia.setDataRegistro(dataDeHoje());          // data automática
        ocorrencia.setStatus(Ocorrencia.STATUS_ABERTA);    // status inicial
        ocorrencia.setDataDiagnostico(null);               // ainda não agendada
        // Valor fixo do diagnóstico do mecânico, gerado automaticamente.
        ocorrencia.setValorDiagnostico(Ocorrencia.VALOR_DIAGNOSTICO);

        long id = bd.inserirOcorrencia(ocorrencia);
        if (id != -1) {
            Toast.makeText(this, "Ocorrência registrada\nDiagnóstico: "
                    + formatarValor(Ocorrencia.VALOR_DIAGNOSTICO), Toast.LENGTH_LONG).show();
            etDescricao.setText("");
            recarregarLista();
        } else {
            Toast.makeText(this, "Erro ao registrar", Toast.LENGTH_SHORT).show();
        }
    }

    /** Devolve a data de hoje no formato dd/MM/yyyy. */
    private String dataDeHoje() {
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return formato.format(new Date());
    }

    /** Formata um valor em reais no padrão brasileiro (ex.: R$ 150,00). */
    private String formatarValor(double valor) {
        return String.format(new Locale("pt", "BR"), "R$ %.2f", valor);
    }

    /** Lixeira: confirma com AlertDialog e exclui. */
    @Override
    public void onExcluirOcorrencia(final Ocorrencia ocorrencia) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir ocorrência")
                .setMessage("Deseja excluir esta ocorrência?")
                .setPositiveButton("Excluir", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        bd.excluirOcorrencia(ocorrencia.getId());
                        Toast.makeText(OcorrenciasActivity.this, "Ocorrência excluída",
                                Toast.LENGTH_SHORT).show();
                        recarregarLista();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void recarregarLista() {
        ocorrencias.clear();
        ocorrencias.addAll(bd.listarOcorrencias());
        adapter.notifyDataSetChanged();
    }

    /** Ao voltar para a tela, atualiza o Spinner (pode ter veículo novo). */
    @Override
    protected void onResume() {
        super.onResume();
        carregarVeiculosNoSpinner();
    }
}
