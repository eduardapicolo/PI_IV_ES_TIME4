package br.com.salus;

public class PedidoExcluirCompeticao extends Comunicado {
    private String idCompeticao;
    private String idUsuario;

    static final long serialVersionUID = 23L;

    public PedidoExcluirCompeticao(String idCompeticao, String idUsuario) {
        this.idCompeticao = idCompeticao;
        this.idUsuario = idUsuario;
    }

    public String getIdCompeticao() {
        return idCompeticao;
    }

    public String getIdUsuario() {
        return idUsuario;
    }
}