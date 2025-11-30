package br.com.salus;

public class PedidoBuscaUsuario extends Comunicado {
    private static final long serialVersionUID = 500L;

    private String userId;

    public PedidoBuscaUsuario(String userId) throws Exception {
        if (userId == null) { throw new Exception("Id ausente."); }

        this.userId = userId;
    }

    public String getUserId() { return this.userId; }
}
