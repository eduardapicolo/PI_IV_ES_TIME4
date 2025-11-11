package br.com.salus;

public class RespostaDeLogin extends Resposta {
    private static final long serialVersionUID = 13L;
    private String userId;

    public RespostaDeLogin(boolean sucesso, String msg, String userId) {
        super(sucesso, msg);
        this.userId = userId;
    }

    public RespostaDeLogin(boolean sucesso, String msg) {
        super(sucesso, msg);
        this.userId = null;
    }

    public String getUserId() {
        return this.userId;
    }
}
