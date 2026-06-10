package br.com.posvendas.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import br.com.posvendas.DatabaseHelper;
import br.com.posvendas.R;
import br.com.posvendas.model.Ocorrencia;

/**
 * Painel/Dashboard: tela só de leitura com um resumo do sistema.
 * Mostra contadores vindos do banco (clientes, veículos e ocorrências por
 * status). Os números são recalculados em onResume, então sempre refletem o
 * estado atual ao voltar para esta tela.
 */
public class DashboardActivity extends AppCompatActivity {

    private DatabaseHelper bd;

    private TextView tvTotalOcorrencias;
    private TextView tvQtdClientes;
    private TextView tvQtdVeiculos;
    private TextView tvQtdAbertas;
    private TextView tvQtdAgendadas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Toolbar com seta de voltar: encerra a tela e retorna ao menu.
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        bd = new DatabaseHelper(this);

        tvTotalOcorrencias = findViewById(R.id.tvTotalOcorrencias);
        tvQtdClientes = findViewById(R.id.tvQtdClientes);
        tvQtdVeiculos = findViewById(R.id.tvQtdVeiculos);
        tvQtdAbertas = findViewById(R.id.tvQtdAbertas);
        tvQtdAgendadas = findViewById(R.id.tvQtdAgendadas);

        Button btnAtualizar = findViewById(R.id.btnAtualizarPainel);
        btnAtualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                atualizarContadores();
            }
        });
    }

    /** Recalcula os números sempre que a tela volta ao primeiro plano. */
    @Override
    protected void onResume() {
        super.onResume();
        atualizarContadores();
    }

    /** Lê as contagens do banco e atualiza os cartões. */
    private void atualizarContadores() {
        int clientes = bd.listarClientes().size();
        int veiculos = bd.listarVeiculos().size();
        int abertas = bd.listarOcorrenciasPorStatus(Ocorrencia.STATUS_ABERTA).size();
        int agendadas = bd.listarOcorrenciasPorStatus(Ocorrencia.STATUS_AGENDADA).size();
        int total = bd.listarOcorrencias().size();

        tvQtdClientes.setText(String.valueOf(clientes));
        tvQtdVeiculos.setText(String.valueOf(veiculos));
        tvQtdAbertas.setText(String.valueOf(abertas));
        tvQtdAgendadas.setText(String.valueOf(agendadas));
        tvTotalOcorrencias.setText(String.valueOf(total));
    }
}
