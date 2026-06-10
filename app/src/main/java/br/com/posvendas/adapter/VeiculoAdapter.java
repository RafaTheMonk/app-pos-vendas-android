package br.com.posvendas.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

import br.com.posvendas.R;
import br.com.posvendas.model.Veiculo;

/**
 * Adapter da lista de veículos.
 * Além dos textos, cada linha mostra a MINIATURA da foto carregada do caminho
 * salvo no banco (coluna foto_caminho). Se não houver foto, mostra um ícone padrão.
 */
public class VeiculoAdapter extends RecyclerView.Adapter<VeiculoAdapter.VeiculoViewHolder> {

    public interface OnVeiculoAcaoListener {
        void onEditarVeiculo(Veiculo veiculo);
        void onExcluirVeiculo(Veiculo veiculo);
    }

    private final List<Veiculo> veiculos;
    private final OnVeiculoAcaoListener listener;

    public VeiculoAdapter(List<Veiculo> veiculos, OnVeiculoAcaoListener listener) {
        this.veiculos = veiculos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VeiculoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_veiculo, parent, false);
        return new VeiculoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VeiculoViewHolder holder, int position) {
        final Veiculo veiculo = veiculos.get(position);

        holder.tvModeloPlaca.setText(veiculo.getModelo() + " (" + veiculo.getAno() + ")");
        holder.tvPlaca.setText("Placa: " + veiculo.getPlaca());
        holder.tvDono.setText("Dono: " + veiculo.getClienteNome());

        // Carrega a miniatura a partir do caminho salvo no banco.
        carregarMiniatura(holder.ivMiniatura, veiculo.getFotoCaminho());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onEditarVeiculo(veiculo);
            }
        });

        holder.btnExcluir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onExcluirVeiculo(veiculo);
            }
        });
    }

    /**
     * Lê o arquivo de imagem do caminho informado e mostra no ImageView.
     * Se o caminho estiver vazio ou o arquivo não existir, mostra ícone padrão.
     */
    private void carregarMiniatura(ImageView imageView, String caminho) {
        if (!TextUtils.isEmpty(caminho)) {
            File arquivo = new File(caminho);
            if (arquivo.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(caminho);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    return;
                }
            }
        }
        // Sem foto válida: ícone genérico de carro.
        imageView.setImageResource(R.drawable.ic_veiculo);
    }

    @Override
    public int getItemCount() {
        return veiculos.size();
    }

    static class VeiculoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMiniatura;
        TextView tvModeloPlaca;
        TextView tvPlaca;
        TextView tvDono;
        ImageButton btnExcluir;

        VeiculoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMiniatura = itemView.findViewById(R.id.ivMiniatura);
            tvModeloPlaca = itemView.findViewById(R.id.tvModeloPlaca);
            tvPlaca = itemView.findViewById(R.id.tvPlacaItem);
            tvDono = itemView.findViewById(R.id.tvDono);
            btnExcluir = itemView.findViewById(R.id.btnExcluirVeiculo);
        }
    }
}
