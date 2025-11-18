package br.com.salus;

public class Resposta extends Comunicado {

    private static final long serialVersionUID = 3L;
    private boolean sucesso;
    private String mensagem;

    public Resposta(boolean sucesso, String mensagem) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
    }

    public boolean getSucesso () {
        return this.sucesso;
    }

    public String getMensagem () {
        return this.mensagem;
    }

    @Override
    public String toString () {
        return "sucesso: " + this.sucesso + " mensagem: " + this.mensagem;
    }
}
