package br.com.salus;

public class PedidoEdicaoCompeticao extends Comunicado {
    private static final long serialVersionUID = 120L;

    private String idCompeticao;
    private String novoNome;
    private Integer novoIdIcone;

    public PedidoEdicaoCompeticao(String idCompeticao, String novoNome, Integer novoIdIcone) {
        this.idCompeticao = idCompeticao;
        this.novoNome = novoNome;
        this.novoIdIcone = novoIdIcone;
    }

    public String getIdCompeticao() {
        return idCompeticao;
    }

    public String getNovoNome() {
        return novoNome;
    }

    public Integer getNovoIdIcone() {
        return novoIdIcone;
    }
}