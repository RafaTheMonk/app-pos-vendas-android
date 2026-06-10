package br.com.posvendas.model;

/**
 * POJO simples que representa um registro da tabela "ocorrencias".
 * Os campos clienteNome, veiculoModelo e placa NÃO existem na tabela:
 * são preenchidos por SELECT com JOIN para exibir na Agenda e no Relatório.
 */
public class Ocorrencia {

    // Constantes de status para evitar erro de digitação no código.
    public static final String STATUS_ABERTA = "ABERTA";
    public static final String STATUS_AGENDADA = "AGENDADA";

    // Valor fixo cobrado pelo diagnóstico do mecânico em toda ocorrência.
    public static final double VALOR_DIAGNOSTICO = 150.0;

    private int id;
    private int veiculoId;
    private String descricao;
    private String dataRegistro;    // formato dd/MM/yyyy (data atual no INSERT)
    private String status;          // ABERTA ou AGENDADA
    private String dataDiagnostico; // formato dd/MM/yyyy (null enquanto não agendada)
    private double valorDiagnostico; // R$ fixo do diagnóstico do mecânico

    // Campos de apoio (vêm do JOIN, não são colunas da tabela)
    private String clienteNome;
    private String veiculoModelo;
    private String placa;

    public Ocorrencia() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVeiculoId() {
        return veiculoId;
    }

    public void setVeiculoId(int veiculoId) {
        this.veiculoId = veiculoId;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(String dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDataDiagnostico() {
        return dataDiagnostico;
    }

    public void setDataDiagnostico(String dataDiagnostico) {
        this.dataDiagnostico = dataDiagnostico;
    }

    public double getValorDiagnostico() {
        return valorDiagnostico;
    }

    public void setValorDiagnostico(double valorDiagnostico) {
        this.valorDiagnostico = valorDiagnostico;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public void setClienteNome(String clienteNome) {
        this.clienteNome = clienteNome;
    }

    public String getVeiculoModelo() {
        return veiculoModelo;
    }

    public void setVeiculoModelo(String veiculoModelo) {
        this.veiculoModelo = veiculoModelo;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }
}
