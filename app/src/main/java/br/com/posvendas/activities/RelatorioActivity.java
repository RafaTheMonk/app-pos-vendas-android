package br.com.posvendas.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import br.com.posvendas.DatabaseHelper;
import br.com.posvendas.R;
import br.com.posvendas.model.Ocorrencia;

/**
 * Tela de Relatório (.txt).
 * - Gerar Relatório: percorre as ocorrências (SELECT) e grava um arquivo
 *   relatorio.txt no armazenamento interno usando FileOutputStream.
 * - Carregar Relatório: lê o arquivo com FileInputStream e mostra no TextView.
 * O arquivo fica em getFilesDir(), então não precisa de permissão de armazenamento.
 */
public class RelatorioActivity extends AppCompatActivity {

    // Nome do arquivo de relatório.
    private static final String NOME_ARQUIVO = "relatorio.txt";

    private TextView tvConteudo;
    private DatabaseHelper bd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorio);

        bd = new DatabaseHelper(this);

        tvConteudo = findViewById(R.id.tvConteudoRelatorio);
        Button btnGerar = findViewById(R.id.btnGerarRelatorio);
        Button btnCarregar = findViewById(R.id.btnCarregarRelatorio);

        btnGerar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gerarRelatorio();
            }
        });

        btnCarregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                carregarRelatorio();
            }
        });
    }

    /**
     * GERAR: monta o texto a partir das ocorrências e grava em arquivo
     * usando FileOutputStream (escrita byte a byte do conteúdo de texto).
     */
    private void gerarRelatorio() {
        List<Ocorrencia> ocorrencias = bd.listarOcorrencias();

        // Monta todo o conteúdo do relatório em um StringBuilder.
        StringBuilder conteudo = new StringBuilder();
        conteudo.append("===== RELATÓRIO DE OCORRÊNCIAS =====\n\n");

        if (ocorrencias.isEmpty()) {
            conteudo.append("Nenhuma ocorrência registrada.\n");
        } else {
            for (Ocorrencia o : ocorrencias) {
                String diagnostico = (o.getDataDiagnostico() == null) ? "-" : o.getDataDiagnostico();
                conteudo.append("Cliente: ").append(o.getClienteNome())
                        .append(" | Veículo: ").append(o.getVeiculoModelo())
                        .append(" | Problema: ").append(o.getDescricao())
                        .append(" | Status: ").append(o.getStatus())
                        .append(" | Diagnóstico: ").append(diagnostico)
                        .append("\n");
            }
        }

        // Grava o arquivo no armazenamento interno do app.
        File arquivo = new File(getFilesDir(), NOME_ARQUIVO);
        try (FileOutputStream fos = new FileOutputStream(arquivo)) {
            fos.write(conteudo.toString().getBytes());
            Toast.makeText(this, "Relatório gerado com sucesso", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Erro ao gerar relatório", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * CARREGAR: lê o arquivo com FileInputStream e exibe no TextView.
     * Se o arquivo ainda não existir, avisa o usuário (tratamento de erro).
     */
    private void carregarRelatorio() {
        File arquivo = new File(getFilesDir(), NOME_ARQUIVO);

        // Trata o caso de o arquivo não existir (ainda não foi gerado).
        if (!arquivo.exists()) {
            Toast.makeText(this, "Nenhum relatório encontrado. Gere primeiro.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        StringBuilder conteudo = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(arquivo)) {
            int caractere;
            // Lê byte a byte até o fim do arquivo (-1).
            while ((caractere = fis.read()) != -1) {
                conteudo.append((char) caractere);
            }
            tvConteudo.setText(conteudo.toString());
            Toast.makeText(this, "Relatório carregado", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Erro ao ler relatório", Toast.LENGTH_SHORT).show();
        }
    }
}
