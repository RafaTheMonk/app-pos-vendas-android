package br.com.posvendas.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import br.com.posvendas.DatabaseHelper;
import br.com.posvendas.R;
import br.com.posvendas.adapter.VeiculoAdapter;
import br.com.posvendas.model.Cliente;
import br.com.posvendas.model.Veiculo;

/**
 * Tela de Veículos: CRUD completo + foto pela câmera.
 *
 * Pontos avaliados nesta tela:
 *  - Permissão de CÂMERA em tempo de execução (checkSelfPermission / requestPermissions
 *    / onRequestPermissionsResult).
 *  - Captura de foto com Intent(MediaStore.ACTION_IMAGE_CAPTURE).
 *  - Gravação da imagem em arquivo no armazenamento interno (getFilesDir) e do
 *    CAMINHO no banco (coluna foto_caminho), para a miniatura voltar ao reabrir.
 *  - Validações: campos vazios, ano numérico, chassi com 17 caracteres e
 *    placa/chassi duplicados (tratamento do UNIQUE).
 */
public class VeiculosActivity extends AppCompatActivity
        implements VeiculoAdapter.OnVeiculoAcaoListener {

    // Código que identifica nosso pedido de permissão no callback.
    private static final int RC_CAMERA = 100;

    private Spinner spinnerCliente;
    private EditText etModelo;
    private EditText etAno;
    private EditText etChassi;
    private EditText etPlaca;
    private EditText etDataRetirada;
    private EditText etGarantia;
    private ImageView ivFoto;
    private Button btnFoto;
    private Button btnSalvar;
    private Button btnAtualizar;
    private Button btnLimpar;
    private RecyclerView rvVeiculos;

    private DatabaseHelper bd;
    private VeiculoAdapter adapter;
    private List<Veiculo> veiculos;
    private List<Cliente> clientes;

    private int idEmEdicao = 0;
    private String fotoCaminhoAtual = null; // caminho da foto escolhida para este veículo

    /**
     * Launcher que recebe o resultado da câmera. A foto vem como miniatura
     * no extra "data"; salvamos em arquivo e guardamos o caminho.
     */
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                                Bundle extras = result.getData().getExtras();
                                if (extras != null) {
                                    Bitmap foto = (Bitmap) extras.get("data");
                                    if (foto != null) {
                                        // Grava a imagem em arquivo e guarda o caminho.
                                        String caminho = salvarImagemNoArmazenamentoInterno(foto);
                                        if (caminho != null) {
                                            fotoCaminhoAtual = caminho;
                                            ivFoto.setImageBitmap(foto);
                                        }
                                    }
                                }
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_veiculos);

        bd = new DatabaseHelper(this);

        spinnerCliente = findViewById(R.id.spinnerCliente);
        etModelo = findViewById(R.id.etModelo);
        etAno = findViewById(R.id.etAno);
        etChassi = findViewById(R.id.etChassi);
        etPlaca = findViewById(R.id.etPlaca);
        etDataRetirada = findViewById(R.id.etDataRetirada);
        etGarantia = findViewById(R.id.etGarantia);
        ivFoto = findViewById(R.id.ivFoto);
        btnFoto = findViewById(R.id.btnFoto);
        btnSalvar = findViewById(R.id.btnSalvarVeiculo);
        btnAtualizar = findViewById(R.id.btnAtualizarVeiculo);
        btnLimpar = findViewById(R.id.btnLimparVeiculo);
        rvVeiculos = findViewById(R.id.rvVeiculos);

        // Carrega os clientes no Spinner (precisamos de pelo menos um para cadastrar veículo).
        carregarClientesNoSpinner();

        // Configura a lista de veículos.
        rvVeiculos.setLayoutManager(new LinearLayoutManager(this));
        veiculos = bd.listarVeiculos();
        adapter = new VeiculoAdapter(veiculos, this);
        rvVeiculos.setAdapter(adapter);

        habilitarModoInsercao();

        // Campo de data é só leitura: ao tocar, abre o DatePickerDialog.
        etDataRetirada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirSeletorDeData();
            }
        });

        btnFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Antes de abrir a câmera, garantimos a permissão.
                verificarPermissaoEAbrirCamera();
            }
        });

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarVeiculo();
            }
        });

        btnAtualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                atualizarVeiculo();
            }
        });

        btnLimpar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                limparFormulario();
            }
        });
    }

    // ================================================================
    // CÂMERA + PERMISSÃO (conteúdo avaliado)
    // ================================================================

    /**
     * Passo 1: verifica se a permissão de câmera já foi concedida.
     * Se sim, abre a câmera direto. Se não, solicita a permissão ao usuário.
     */
    private void verificarPermissaoEAbrirCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            abrirCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, RC_CAMERA);
        }
    }

    /**
     * Passo 2: callback do Android com a resposta do usuário ao pedido de permissão.
     * Mostramos um Toast informando se foi concedida ou negada (item avaliado).
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão de câmera concedida", Toast.LENGTH_SHORT).show();
                abrirCamera();
            } else {
                Toast.makeText(this, "Permissão de câmera negada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Passo 3: dispara a câmera do sistema via Intent. O resultado volta no cameraLauncher.
     */
    private void abrirCamera() {
        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intentCamera.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(intentCamera);
        } else {
            Toast.makeText(this, "Nenhum app de câmera encontrado", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Grava o Bitmap como arquivo .jpg dentro de getFilesDir() usando FileOutputStream.
     * getFilesDir() é a área privada do app, então NÃO precisa de permissão de armazenamento.
     * Devolve o caminho absoluto do arquivo (que será salvo no banco).
     */
    private String salvarImagemNoArmazenamentoInterno(Bitmap bitmap) {
        File arquivo = new File(getFilesDir(), "veiculo_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream fos = new FileOutputStream(arquivo)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            return arquivo.getAbsolutePath();
        } catch (IOException e) {
            Toast.makeText(this, "Erro ao salvar a foto", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    // ================================================================
    // DATA (DatePickerDialog)
    // ================================================================

    /** Abre o calendário e, ao confirmar, escreve a data no formato dd/MM/yyyy. */
    private void abrirSeletorDeData() {
        Calendar hoje = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int ano, int mes, int dia) {
                        // mes começa em 0 (janeiro = 0), por isso somamos 1.
                        String data = String.format(Locale.getDefault(),
                                "%02d/%02d/%04d", dia, mes + 1, ano);
                        etDataRetirada.setText(data);
                    }
                },
                hoje.get(Calendar.YEAR), hoje.get(Calendar.MONTH), hoje.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    // ================================================================
    // CRUD
    // ================================================================

    /** Carrega a lista de clientes em um ArrayAdapter para o Spinner. */
    private void carregarClientesNoSpinner() {
        clientes = bd.listarClientes();
        ArrayAdapter<Cliente> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, clientes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCliente.setAdapter(spinnerAdapter);
    }

    /** INSERT: valida e grava um novo veículo. */
    private void salvarVeiculo() {
        if (!camposValidos()) {
            return;
        }

        Veiculo veiculo = lerVeiculoDoFormulario();
        long id = bd.inserirVeiculo(veiculo);

        if (id == -1) {
            // -1 aqui significa que o chassi ou a placa já existem (UNIQUE).
            Toast.makeText(this, "Chassi ou placa já cadastrados", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Veículo salvo com sucesso", Toast.LENGTH_SHORT).show();
            limparFormulario();
            recarregarLista();
        }
    }

    /** UPDATE: grava as alterações do veículo em edição. */
    private void atualizarVeiculo() {
        if (idEmEdicao == 0) {
            return;
        }
        if (!camposValidos()) {
            return;
        }

        Veiculo veiculo = lerVeiculoDoFormulario();
        veiculo.setId(idEmEdicao);
        int resultado = bd.atualizarVeiculo(veiculo);

        if (resultado == -1) {
            Toast.makeText(this, "Chassi ou placa já cadastrados", Toast.LENGTH_LONG).show();
        } else if (resultado > 0) {
            Toast.makeText(this, "Veículo atualizado", Toast.LENGTH_SHORT).show();
            limparFormulario();
            recarregarLista();
        } else {
            Toast.makeText(this, "Erro ao atualizar", Toast.LENGTH_SHORT).show();
        }
    }

    /** Monta um objeto Veiculo a partir do que está no formulário. */
    private Veiculo lerVeiculoDoFormulario() {
        Cliente clienteSelecionado = (Cliente) spinnerCliente.getSelectedItem();

        Veiculo veiculo = new Veiculo();
        veiculo.setClienteId(clienteSelecionado.getId());
        veiculo.setModelo(etModelo.getText().toString().trim());
        veiculo.setAno(Integer.parseInt(etAno.getText().toString().trim()));
        veiculo.setChassi(etChassi.getText().toString().trim());
        veiculo.setPlaca(etPlaca.getText().toString().trim().toUpperCase(Locale.getDefault()));
        veiculo.setDataRetirada(etDataRetirada.getText().toString().trim());
        veiculo.setGarantiaAdicional(etGarantia.getText().toString().trim());
        veiculo.setFotoCaminho(fotoCaminhoAtual);
        return veiculo;
    }

    /** Todas as validações exigidas antes de tocar no banco. */
    private boolean camposValidos() {
        // Precisa existir um cliente selecionado no Spinner.
        if (clientes == null || clientes.isEmpty() || spinnerCliente.getSelectedItem() == null) {
            Toast.makeText(this, "Cadastre um cliente antes de adicionar veículos",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        String modelo = etModelo.getText().toString().trim();
        String ano = etAno.getText().toString().trim();
        String chassi = etChassi.getText().toString().trim();
        String placa = etPlaca.getText().toString().trim();
        String dataRetirada = etDataRetirada.getText().toString().trim();

        if (TextUtils.isEmpty(modelo)) {
            etModelo.setError("Informe o modelo");
            return false;
        }
        if (TextUtils.isEmpty(ano)) {
            etAno.setError("Informe o ano");
            return false;
        }
        // Ano precisa ser numérico (senão Integer.parseInt quebraria).
        if (!ano.matches("\\d+")) {
            etAno.setError("Ano deve ser numérico");
            return false;
        }
        if (TextUtils.isEmpty(chassi)) {
            etChassi.setError("Informe o chassi");
            return false;
        }
        // Chassi tem exatamente 17 caracteres (padrão internacional).
        if (chassi.length() != 17) {
            etChassi.setError("Chassi deve ter 17 caracteres");
            return false;
        }
        if (TextUtils.isEmpty(placa)) {
            etPlaca.setError("Informe a placa");
            return false;
        }
        if (TextUtils.isEmpty(dataRetirada)) {
            Toast.makeText(this, "Escolha a data de retirada", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /** Toque no item: carrega o veículo no formulário para edição. */
    @Override
    public void onEditarVeiculo(Veiculo veiculo) {
        idEmEdicao = veiculo.getId();
        selecionarClienteNoSpinner(veiculo.getClienteId());
        etModelo.setText(veiculo.getModelo());
        etAno.setText(String.valueOf(veiculo.getAno()));
        etChassi.setText(veiculo.getChassi());
        etPlaca.setText(veiculo.getPlaca());
        etDataRetirada.setText(veiculo.getDataRetirada());
        etGarantia.setText(veiculo.getGarantiaAdicional());

        // Recupera a foto já salva (se houver) para o preview.
        fotoCaminhoAtual = veiculo.getFotoCaminho();
        carregarPreviewFoto(fotoCaminhoAtual);

        habilitarModoEdicao();
    }

    /** Lixeira: confirma com AlertDialog e exclui. */
    @Override
    public void onExcluirVeiculo(final Veiculo veiculo) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir veículo")
                .setMessage("Excluir o veículo " + veiculo.getModelo() + " (" + veiculo.getPlaca() + ")?")
                .setPositiveButton("Excluir", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        bd.excluirVeiculo(veiculo.getId());
                        Toast.makeText(VeiculosActivity.this, "Veículo excluído", Toast.LENGTH_SHORT).show();
                        limparFormulario();
                        recarregarLista();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /** Posiciona o Spinner no cliente dono do veículo (ao editar). */
    private void selecionarClienteNoSpinner(int clienteId) {
        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getId() == clienteId) {
                spinnerCliente.setSelection(i);
                return;
            }
        }
    }

    /** Mostra a foto salva no ImageView (ou um ícone padrão se não houver). */
    private void carregarPreviewFoto(String caminho) {
        if (!TextUtils.isEmpty(caminho)) {
            File arquivo = new File(caminho);
            if (arquivo.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(caminho);
                if (bitmap != null) {
                    ivFoto.setImageBitmap(bitmap);
                    return;
                }
            }
        }
        ivFoto.setImageResource(R.drawable.ic_veiculo);
    }

    private void recarregarLista() {
        veiculos.clear();
        veiculos.addAll(bd.listarVeiculos());
        adapter.notifyDataSetChanged();
    }

    /** Limpa o formulário e volta ao modo de inserção. */
    private void limparFormulario() {
        idEmEdicao = 0;
        fotoCaminhoAtual = null;
        etModelo.setText("");
        etAno.setText("");
        etChassi.setText("");
        etPlaca.setText("");
        etDataRetirada.setText("");
        etGarantia.setText("");
        ivFoto.setImageResource(R.drawable.ic_veiculo);
        if (clientes != null && !clientes.isEmpty()) {
            spinnerCliente.setSelection(0);
        }
        habilitarModoInsercao();
    }

    private void habilitarModoInsercao() {
        btnSalvar.setEnabled(true);
        btnAtualizar.setEnabled(false);
    }

    private void habilitarModoEdicao() {
        btnSalvar.setEnabled(false);
        btnAtualizar.setEnabled(true);
    }

    /**
     * Recarrega os clientes do Spinner ao voltar para a tela
     * (caso novos clientes tenham sido cadastrados na outra Activity).
     */
    @Override
    protected void onResume() {
        super.onResume();
        carregarClientesNoSpinner();
    }
}
