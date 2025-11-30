package br.com.salus;

public class PedidoDeletarConta extends Comunicado{

    private static final long serialVersionUID = 106L;

    private String idUsuario;


    public PedidoDeletarConta (String idUsuario) throws Exception {

        if (idUsuario == null || idUsuario.isEmpty()) {
            throw new Exception("Id do usario é necessario para identificar a conta");
        }

        this.idUsuario = idUsuario;
    }

    public String getIdUsuario() { return this.idUsuario; }

}
