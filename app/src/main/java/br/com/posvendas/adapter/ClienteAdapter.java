package br.com.posvendas.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.posvendas.R;
import br.com.posvendas.model.Cliente;

/**
 * Adapter da lista (RecyclerView) de clientes.
 * Cada item mostra nome + contato e tem um botão de excluir.
 * O toque no item dispara a edição; o botão da lixeira dispara a exclusão.
 * A Activity implementa a interface abaixo para reagir a esses dois eventos.
 */
public class ClienteAdapter extends RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder> {

    // Contrato que a Activity precisa cumprir para tratar os cliques.
    public interface OnClienteAcaoListener {
        void onEditarCliente(Cliente cliente);
        void onExcluirCliente(Cliente cliente);
    }

    private final List<Cliente> clientes;
    private final OnClienteAcaoListener listener;

    public ClienteAdapter(List<Cliente> clientes, OnClienteAcaoListener listener) {
        this.clientes = clientes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla o layout de UMA linha da lista.
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cliente, parent, false);
        return new ClienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClienteViewHolder holder, int position) {
        // Pega o cliente desta posição e joga os dados nos campos da linha.
        final Cliente cliente = clientes.get(position);

        holder.tvNome.setText(cliente.getNome());
        holder.tvContato.setText(cliente.getTelefone() + "  •  " + cliente.getEmail());

        // Toque na linha inteira = editar.
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onEditarCliente(cliente);
            }
        });

        // Botão lixeira = excluir.
        holder.btnExcluir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onExcluirCliente(cliente);
            }
        });
    }

    @Override
    public int getItemCount() {
        return clientes.size();
    }

    /** ViewHolder guarda as referências das Views de uma linha (evita findViewById repetido). */
    static class ClienteViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome;
        TextView tvContato;
        ImageButton btnExcluir;

        ClienteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNome);
            tvContato = itemView.findViewById(R.id.tvContato);
            btnExcluir = itemView.findViewById(R.id.btnExcluirCliente);
        }
    }
}
