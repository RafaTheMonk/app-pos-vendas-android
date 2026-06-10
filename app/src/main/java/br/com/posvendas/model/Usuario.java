package br.com.posvendas.model;

/**
 * POJO simples que representa um registro da tabela "usuarios".
 * Usado apenas para transportar dados entre o banco e as telas.
 */
public class Usuario {

    private int id;
    private String login;
    private String senha;
    private String perfil; // ADMIN, MECANICO, etc.

    public Usuario() {
    }

    public Usuario(int id, String login, String senha, String perfil) {
        this.id = id;
        this.login = login;
        this.senha = senha;
        this.perfil = perfil;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }
}
