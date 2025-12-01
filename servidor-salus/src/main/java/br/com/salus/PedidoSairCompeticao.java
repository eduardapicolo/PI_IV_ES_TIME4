package br.com.salus;


public class PedidoSairCompeticao extends Comunicado {
    private String idCompeticao;
    private String idUsuario;

    static final long serialVersionUID = 24L;

    public PedidoSairCompeticao(String idCompeticao, String idUsuario) {
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