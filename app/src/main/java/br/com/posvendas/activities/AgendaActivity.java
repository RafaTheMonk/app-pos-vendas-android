package br.com.posvendas.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import br.com.posvendas.DatabaseHelper;
import br.com.posvendas.R;
import br.com.posvendas.adapter.AgendaAdapter;
import br.com.posvendas.model.Ocorrencia;

/**
 * Tela da Agenda (visão do mecânico).
 * Mostra duas listas vindas de SELECT com JOIN nas 3 tabelas:
 *  - Ocorrências ABERTAS: cada uma com botão "Agendar".
 *  - Ocorrências AGENDADAS: com a data do diagnóstico já marcada.
 * Agendar = DatePickerDialog -> UPDATE (data_diagnostico + status AGENDADA).
 */
public class AgendaActivity extends AppCompatActivity
        implements AgendaAdapter.OnAgendarListener {

    private RecyclerView rvAbertas;
    private RecyclerView rvAgendadas;

    private DatabaseHelper bd;

    private AgendaAdapter adapterAbertas;
    private AgendaAdapter adapterAgendadas;
    private List<Ocorrencia> abertas;
    private List<Ocorrencia> agendadas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);

        bd = new DatabaseHelper(this);

        rvAbertas = findViewById(R.id.rvAbertas);
        rvAgendadas = findViewById(R.id.rvAgendadas);

        rvAbertas.setLayoutManager(new LinearLayoutManager(this));
        rvAgendadas.setLayoutManager(new LinearLayoutManager(this));

        // Lista das abertas (com botão Agendar).
        abertas = bd.listarOcorrenciasPorStatus(Ocorrencia.STATUS_ABERTA);
        adapterAbertas = new AgendaAdapter(abertas, this);
        rvAbertas.setAdapter(adapterAbertas);

        // Lista das já agendadas (somente leitura, mostra a data).
        agendadas = bd.listarOcorrenciasPorStatus(Ocorrencia.STATUS_AGENDADA);
        adapterAgendadas = new AgendaAdapter(agendadas, this);
        rvAgendadas.setAdapter(adapterAgendadas);
    }

    /** Botão Agendar do item: abre o calendário para escolher a data do diagnóstico. */
    @Override
    public void onAgendar(final Ocorrencia ocorrencia) {
        Calendar hoje = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int ano, int mes, int dia) {
                        String data = String.format(Locale.getDefault(),
                                "%02d/%02d/%04d", dia, mes + 1, ano);
                        // UPDATE: grava a data e muda o status para AGENDADA.
                        bd.agendarDiagnostico(ocorrencia.getId(), data);
                        recarregarListas();
                    }
                },
                hoje.get(Calendar.YEAR), hoje.get(Calendar.MONTH), hoje.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    /** Recarrega as duas listas a partir do banco. */
    private void recarregarListas() {
        abertas.clear();
        abertas.addAll(bd.listarOcorrenciasPorStatus(Ocorrencia.STATUS_ABERTA));
        adapterAbertas.notifyDataSetChanged();

        agendadas.clear();
        agendadas.addAll(bd.listarOcorrenciasPorStatus(Ocorrencia.STATUS_AGENDADA));
        adapterAgendadas.notifyDataSetChanged();
    }
}
