package br.com.posvendas.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.posvendas.R;
import br.com.posvendas.model.Ocorrencia;

/**
 * Adapter da lista de ocorrências.
 * Mostra veículo/cliente, descrição e o STATUS com cor diferente
 * (ABERTA em laranja, AGENDADA em verde). Tem botão de excluir.
 */
public class OcorrenciaAdapter extends RecyclerView.Adapter<OcorrenciaAdapter.OcorrenciaViewHolder> {

    public interface OnOcorrenciaAcaoListener {
        void onExcluirOcorrencia(Ocorrencia ocorrencia);
    }

    private final List<Ocorrencia> ocorrencias;
    private final OnOcorrenciaAcaoListener listener;

    public OcorrenciaAdapter(List<Ocorrencia> ocorrencias, OnOcorrenciaAcaoListener listener) {
        this.ocorrencias = ocorrencias;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OcorrenciaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ocorrencia, parent, false);
        return new OcorrenciaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OcorrenciaViewHolder holder, int position) {
        final Ocorrencia ocorrencia = ocorrencias.get(position);

        holder.tvVeiculo.setText(ocorrencia.getVeiculoModelo() + " - " + ocorrencia.getPlaca());
        holder.tvCliente.setText("Cliente: " + ocorrencia.getClienteNome());
        holder.tvDescricao.setText(ocorrencia.getDescricao());
        holder.tvData.setText("Registrada em " + ocorrencia.getDataRegistro());
        holder.tvStatus.setText(ocorrencia.getStatus());

        // Pinta o status conforme o valor: ABERTA = laranja, AGENDADA = verde.
        int cor;
        if (Ocorrencia.STATUS_AGENDADA.equals(ocorrencia.getStatus())) {
            cor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_agendada);
        } else {
            cor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_aberta);
        }
        holder.tvStatus.setTextColor(cor);

        holder.btnExcluir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onExcluirOcorrencia(ocorrencia);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ocorrencias.size();
    }

    static class OcorrenciaViewHolder extends RecyclerView.ViewHolder {
        TextView tvVeiculo;
        TextView tvCliente;
        TextView tvDescricao;
        TextView tvData;
        TextView tvStatus;
        ImageButton btnExcluir;

        OcorrenciaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVeiculo = itemView.findViewById(R.id.tvOcVeiculo);
            tvCliente = itemView.findViewById(R.id.tvOcCliente);
            tvDescricao = itemView.findViewById(R.id.tvOcDescricao);
            tvData = itemView.findViewById(R.id.tvOcData);
            tvStatus = itemView.findViewById(R.id.tvOcStatus);
            btnExcluir = itemView.findViewById(R.id.btnExcluirOcorrencia);
        }
    }
}
