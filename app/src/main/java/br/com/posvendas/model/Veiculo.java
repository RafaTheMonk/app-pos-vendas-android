package br.com.posvendas.model;

/**
 * POJO simples que representa um registro da tabela "veiculos".
 * O campo clienteNome NÃO existe na tabela: ele é preenchido por um SELECT
 * com JOIN apenas para exibir o nome do dono na lista (campo de apoio).
 */
public class Veiculo {

    private int id;
    private int clienteId;
    private String modelo;
    private int ano;
    private String chassi;
    private String placa;
    private String dataRetirada;       // formato dd/MM/yyyy
    private String garantiaAdicional;  // opcional (pode ser null/vazio)
    private String fotoCaminho;        // caminho do arquivo salvo em getFilesDir()

    private String clienteNome;        // campo de apoio (vem do JOIN, não é coluna)

    public Veiculo() {
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
