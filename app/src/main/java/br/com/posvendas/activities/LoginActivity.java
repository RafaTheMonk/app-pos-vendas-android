package br.com.posvendas.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import br.com.posvendas.DatabaseHelper;
import br.com.posvendas.R;
import br.com.posvendas.model.Usuario;

/**
 * Tela de login (primeira tela do app).
 * Faz um SELECT na tabela usuarios para validar login e senha.
 * No primeiro boot já existe o usuário seed: admin / admin123.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etLogin;
    private EditText etSenha;
    private DatabaseHelper bd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Liga os campos do XML às variáveis Java.
        etLogin = findViewById(R.id.etLogin);
        etSenha = findViewById(R.id.etSenha);
        Button btnEntrar = findViewById(R.id.btnEntrar);

        // Abre o banco (cria as tabelas e o usuário seed na primeira vez).
        bd = new DatabaseHelper(this);

        btnEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fazerLogin();
            }
        });
    }

    /** Valida os campos e tenta autenticar consultando o banco. */
    private void fazerLogin() {
        String login = etLogin.getText().toString().trim();
        String senha = etSenha.getText().toString().trim();

        // Validação: nenhum campo pode estar vazio antes de consultar o banco.
        if (TextUtils.isEmpty(login) || TextUtils.isEmpty(senha)) {
            Toast.makeText(this, "Preencha usuário e senha", Toast.LENGTH_SHORT).show();
            return;
        }

        // SELECT no banco: se devolver um Usuario, as credenciais conferem.
        Usuario usuario = bd.validarLogin(login, senha);

        if (usuario != null) {
            Toast.makeText(this, "Bem-vindo, " + usuario.getLogin(), Toast.LENGTH_SHORT).show();
            // Vai para o menu e fecha o login (finish) para não voltar com o botão Voltar.
            startActivity(new Intent(this, MenuActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Usuário ou senha inválidos", Toast.LENGTH_SHORT).show();
        }
    }
}
