package br.com.posvendas.model;

/**
 * POJO simples que representa um registro da tabela "clientes".
 */
public class Cliente {

    private int id;
    private String nome;
    private String telefone;
    private String email;

    public Cliente() {
    }

    public Cliente(int id, String nome, String telefone, String email) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Usado pelo Spinner de clientes: o texto exibido é o nome.
     */
    @Override
    public String toString() {
        return nome;
    }
}
