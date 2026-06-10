package br.com.posvendas.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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

        // Toolbar com seta de voltar: encerra a tela e retorna ao menu.
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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
                String valor = String.format(new java.util.Locale("pt", "BR"),
                        "R$ %.2f", o.getValorDiagnostico());
                conteudo.append("Cliente: ").append(o.getClienteNome())
                        .append(" | Veículo: ").append(o.getVeiculoModelo())
                        .append(" | Problema: ").append(o.getDescricao())
                        .append(" | Status: ").append(o.getStatus())
                        .append(" | Diagnóstico: ").append(diagnostico)
                        .append(" | Valor: ").append(valor)
                        .append("\n");
            }
        }

        // Grava o arquivo no armazenamento interno do app.
        File arquivo = new File(getFilesDir(), NOME_ARQUIVO);
        try (FileOutputStream fos = new FileOutputStream(arquivo)) {
            // UTF-8 explícito: acentos viram múltiplos bytes; sem isso a leitura quebra.
            fos.write(conteudo.toString().getBytes(StandardCharsets.UTF_8));
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
        // InputStreamReader com UTF-8: decodifica caractere a caractere (não byte
        // a byte), senão acentos multi-byte viram lixo na tela.
        try (InputStreamReader reader =
                     new InputStreamReader(new FileInputStream(arquivo), StandardCharsets.UTF_8)) {
            int caractere;
            // Lê caractere a caractere até o fim do arquivo (-1).
            while ((caractere = reader.read()) != -1) {
                conteudo.append((char) caractere);
            }
            tvConteudo.setText(conteudo.toString());
            Toast.makeText(this, "Relatório carregado", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Erro ao ler relatório", Toast.LENGTH_SHORT).show();
        }
    }
}
