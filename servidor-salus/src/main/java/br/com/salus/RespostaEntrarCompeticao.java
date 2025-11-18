package br.com.salus;

public class RespostaEntrarCompeticao extends Resposta {
    private static final long serialVersionUID = 17L;

    private String idCompeticao;
    private String nomeCompeticao;

    public RespostaEntrarCompeticao(boolean sucesso, String mensagem, String idCompeticao, String nomeCompeticao) {
        super(sucesso, mensagem);
        this.idCompeticao = idCompeticao;
        this.nomeCompeticao = nomeCompeticao;
    }

    public RespostaEntrarCompeticao(boolean sucesso, String mensagem) {
        super(sucesso, mensagem);
        this.idCompeticao = null;
        this.nomeCompeticao = null;
    }

    public String getIdCompeticao() { return this.idCompeticao; }
    public String getNomeCompeticao() { return this.nomeCompeticao; }
}
