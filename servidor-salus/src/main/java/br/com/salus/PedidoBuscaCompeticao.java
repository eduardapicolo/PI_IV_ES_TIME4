package br.com.salus;

public class PedidoBuscaCompeticao extends Comunicado {
    private static final long serialVersionUID = 18L;

    private String idUsuario;

    public PedidoBuscaCompeticao(String idUsuario) throws Exception {
        if (idUsuario == null || idUsuario.trim().isEmpty()) {
            throw new Exception("ID do usuário ausente.");
        }
        this.idUsuario = idUsuario;
    }

    public String getIdUsuario() { return this.idUsuario; }

    @Override
    public String toString() { return "PedidoBuscaCompeticao(" + idUsuario + ")"; }
}
