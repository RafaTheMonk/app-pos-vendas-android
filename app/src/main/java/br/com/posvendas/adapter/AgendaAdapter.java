package br.com.posvendas.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.posvendas.R;
import br.com.posvendas.model.Ocorrencia;

/**
 * Adapter usado nas duas listas da Agenda (abertas e agendadas).
 * - Para ocorrências ABERTAS: mostra o botão "Agendar".
 * - Para ocorrências AGENDADAS: esconde o botão e mostra a data marcada.
 * O mesmo adapter serve para os dois casos; quem decide é o status do item.
 */
public class AgendaAdapter extends RecyclerView.Adapter<AgendaAdapter.AgendaViewHolder> {

    public interface OnAgendarListener {
        void onAgendar(Ocorrencia ocorrencia);
    }

    private final List<Ocorrencia> ocorrencias;
    private final OnAgendarListener listener;

    public AgendaAdapter(List<Ocorrencia> ocorrencias, OnAgendarListener listener) {
        this.ocorrencias = ocorrencias;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AgendaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_agenda, parent, false);
        return new AgendaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AgendaViewHolder holder, int position) {
        final Ocorrencia ocorrencia = ocorrencias.get(position);

        holder.tvCliente.setText(ocorrencia.getClienteNome());
        holder.tvVeiculo.setText(ocorrencia.getVeiculoModelo() + " - " + ocorrencia.getPlaca());
        holder.tvDescricao.setText(ocorrencia.getDescricao());

        if (Ocorrencia.STATUS_AGENDADA.equals(ocorrencia.getStatus())) {
            // Já agendada: esconde o botão e mostra a data do diagnóstico.
            holder.btnAgendar.setVisibility(View.GONE);
            holder.tvDataDiagnostico.setVisibility(View.VISIBLE);
            holder.tvDataDiagnostico.setText("Diagnóstico marcado: " + ocorrencia.getDataDiagnostico());
        } else {
            // Ainda aberta: mostra o botão para o mecânico agendar.
            holder.tvDataDiagnostico.setVisibility(View.GONE);
            holder.btnAgendar.setVisibility(View.VISIBLE);
            holder.btnAgendar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onAgendar(ocorrencia);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return ocorrencias.size();
    }

    static class AgendaViewHolder extends RecyclerView.ViewHolder {
        TextView tvCliente;
        TextView tvVeiculo;
        TextView tvDescricao;
        TextView tvDataDiagnostico;
        Button btnAgendar;

        AgendaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCliente = itemView.findViewById(R.id.tvAgCliente);
            tvVeiculo = itemView.findViewById(R.id.tvAgVeiculo);
            tvDescricao = itemView.findViewById(R.id.tvAgDescricao);
            tvDataDiagnostico = itemView.findViewById(R.id.tvAgDataDiagnostico);
            btnAgendar = itemView.findViewById(R.id.btnAgendar);
        }
    }
}
