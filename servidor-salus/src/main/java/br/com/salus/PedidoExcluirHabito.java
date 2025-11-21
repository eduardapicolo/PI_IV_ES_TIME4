package br.com.salus;

public class PedidoExcluirHabito extends Comunicado {
    private static final long serialVersionUID = 22L;

    private String idHabito;
    private String idUsuario;

    public PedidoExcluirHabito(String idHabito, String idUsuario) {
        this.idHabito = idHabito;
        this.idUsuario = idUsuario;
    }

    public String getIdHabito() {
        return idHabito;
    }

    public String getIdUsuario() {
        return idUsuario;
    }
}