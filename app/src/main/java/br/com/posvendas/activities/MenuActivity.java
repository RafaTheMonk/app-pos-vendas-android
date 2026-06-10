package br.com.posvendas.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import br.com.posvendas.R;

/**
 * Menu principal: dá acesso a cada módulo do app.
 * Cada botão só abre a Activity correspondente (navegação simples por Intent).
 */
public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        MaterialButton btnClientes = findViewById(R.id.btnClientes);
        MaterialButton btnVeiculos = findViewById(R.id.btnVeiculos);
        MaterialButton btnOcorrencias = findViewById(R.id.btnOcorrencias);
        MaterialButton btnAgenda = findViewById(R.id.btnAgenda);
        MaterialButton btnRelatorio = findViewById(R.id.btnRelatorio);

        btnClientes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrir(ClientesActivity.class);
            }
        });

        btnVeiculos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrir(VeiculosActivity.class);
            }
        });

        btnOcorrencias.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrir(OcorrenciasActivity.class);
            }
        });

        btnAgenda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrir(AgendaActivity.class);
            }
        });

        btnRelatorio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrir(RelatorioActivity.class);
            }
        });
    }

    /** Atalho para abrir uma Activity sem repetir código. */
    private void abrir(Class<?> activity) {
        startActivity(new Intent(this, activity));
    }
}
