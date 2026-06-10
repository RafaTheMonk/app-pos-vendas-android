package br.com.posvendas.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * POJO simples que representa um registro da tabela "veiculos".
 * O campo clienteNome NÃO existe na tabela: ele é preenchido por um SELECT
 * com JOIN apenas para exibir o nome do dono na lista (campo de apoio).
 */
public class Veiculo {

    // Garantia padrão (em meses) contada a partir da data de retirada.
    public static final int GARANTIA_PADRAO_MESES = 3;
    // Valor fixo cobrado do cliente quando o veículo é cadastrado fora da garantia.
    public static final double VALOR_COBRANCA_FORA_GARANTIA = 300.0;

    private int id;
    private int clienteId;
    private String modelo;
    private int ano;
    private String chassi;
    private String placa;
    private String dataRetirada;       // formato dd/MM/yyyy
    private String garantiaAdicional;  // opcional (pode ser null/vazio)
    private String fotoCaminho;        // caminho do arquivo salvo em getFilesDir()
    private double valorCobranca;      // R$ gerado se cadastrado fora da garantia (0 = na garantia)

    private String clienteNome;        // campo de apoio (vem do JOIN, não é coluna)

    public Veiculo() {
    }

    /**
     * Indica se o veículo está FORA da garantia na data de referência informada.
     * Regra: garantia padrão de {@link #GARANTIA_PADRAO_MESES} meses a partir da
     * data de retirada, somada aos meses extras de garantiaAdicional (quando for
     * um número válido). Se a data de retirada não puder ser lida, devolve false
     * (não cobra por engano).
     */
    public static boolean estaForaDaGarantia(String dataRetirada, String garantiaAdicional,
                                             Date referencia) {
        if (dataRetirada == null || dataRetirada.trim().isEmpty()) {
            return false;
        }
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        formato.setLenient(false);
        try {
            Calendar fim = Calendar.getInstance();
            fim.setTime(formato.parse(dataRetirada.trim()));
            fim.add(Calendar.MONTH, GARANTIA_PADRAO_MESES + mesesAdicionais(garantiaAdicional));
            // Fora da garantia se a referência for posterior ao fim do prazo.
            return referencia.after(fim.getTime());
        } catch (ParseException e) {
            return false;
        }
    }

    /** Converte garantiaAdicional em meses extras; texto inválido/vazio vira 0. */
    private static int mesesAdicionais(String garantiaAdicional) {
        if (garantiaAdicional == null) {
            return 0;
        }
        // Mantém só os dígitos para tolerar entradas como "6 meses".
        String somenteDigitos = garantiaAdicional.replaceAll("[^0-9]", "");
        if (somenteDigitos.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(somenteDigitos);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClienteId() {
        return clienteId;
    }

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public String getChassi() {
        return chassi;
    }

    public void setChassi(String chassi) {
        this.chassi = chassi;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getDataRetirada() {
        return dataRetirada;
    }

    public void setDataRetirada(String dataRetirada) {
        this.dataRetirada = dataRetirada;
    }

    public String getGarantiaAdicional() {
        return garantiaAdicional;
    }

    public void setGarantiaAdicional(String garantiaAdicional) {
        this.garantiaAdicional = garantiaAdicional;
    }

    public String getFotoCaminho() {
        return fotoCaminho;
    }

    public void setFotoCaminho(String fotoCaminho) {
        this.fotoCaminho = fotoCaminho;
    }

    public double getValorCobranca() {
        return valorCobranca;
    }

    public void setValorCobranca(double valorCobranca) {
        this.valorCobranca = valorCobranca;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public void setClienteNome(String clienteNome) {
        this.clienteNome = clienteNome;
    }

    /**
     * Usado pelo Spinner de veículos: mostra modelo e placa para o usuário escolher.
     */
    @Override
    public String toString() {
        return modelo + " - " + placa;
    }
}
