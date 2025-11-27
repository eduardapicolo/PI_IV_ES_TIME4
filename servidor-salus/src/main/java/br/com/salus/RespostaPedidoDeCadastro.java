package br.com.salus;

public class RespostaPedidoDeCadastro extends Resposta {
    private static final long serialVersionUID = 101L;
    private String userId;

    public RespostaPedidoDeCadastro(boolean sucesso, String msg, String userId) {
        super(sucesso, msg);
        this.userId = userId;
    }

    public RespostaPedidoDeCadastro(boolean sucesso, String msg) {
        super(sucesso, msg);
        this.userId = null;
    }

    public String getUserId() {
        return this.userId;
    }
}
