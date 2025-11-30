package br.com.salus;

public class PedidoEdicaoConta extends Comunicado{

    private static final long serialVersionUID = 105L;

    private String idUsuario;
    private String novoApelido;
    private String novoEmail;
    private Integer novoIdFotoPerfil;

    public PedidoEdicaoConta (
            String idUsuario,
            String novoApelido,
            String novoEmail,
            Integer novoIdFotoPerfil
    ) throws Exception {

        if (idUsuario == null || idUsuario.isEmpty()) {
            throw new Exception("Id do usario é necessario para identificar a conta");
        }

        if (novoApelido == null && novoIdFotoPerfil == null && novoEmail == null) {
            throw new Exception("Nenhum dado para alterar foi fornecido.");
        }

        this.idUsuario = idUsuario;
        this.novoApelido = novoApelido;
        this.novoEmail = novoEmail;
        this.novoIdFotoPerfil = novoIdFotoPerfil;
    }

    public String getIdUsuario() { return this.idUsuario; }
    public String getNovoApelido() { return this.novoApelido; }
    public String getNovoEmail() { return novoEmail; }
    public Integer getNovoIdFotoPerfil() { return this.novoIdFotoPerfil; }

}
