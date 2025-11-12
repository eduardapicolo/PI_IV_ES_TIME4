package br.com.salus;

public class PedidoEntrarCompeticao extends Comunicado {
    private static final long serialVersionUID = 16L;

    private String codigo;
    private String idUsuario;

    public PedidoEntrarCompeticao(String codigo, String idUsuario) throws Exception {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new Exception("Código da competição ausente.");
        }
        this.codigo = codigo.trim().toUpperCase();

        if (idUsuario == null || idUsuario.trim().isEmpty()) {
            throw new Exception("ID do usuário ausente.");
        }
        this.idUsuario = idUsuario;
    }

    public String getCodigo() { return this.codigo; }
    public String getIdUsuario() { return this.idUsuario; }

    @Override
    public String toString() {
        return "PedidoEntrarCompeticao [codigo: " + this.codigo + ", idUsuario: " + this.idUsuario + "]";
    }
}