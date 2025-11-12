package br.com.salus;

public class RespostaDeNovaCompeticao extends Resposta {
    private static final long serialVersionUID = 15L;

    private String idCompeticao;
    private String codigo;

    public RespostaDeNovaCompeticao(boolean sucesso, String mensagem, String idCompeticao, String codigo) {
        super(sucesso, mensagem);
        this.idCompeticao = idCompeticao;
        this.codigo = codigo;
    }

    public RespostaDeNovaCompeticao(boolean sucesso, String mensagem) {
        super(sucesso, mensagem);
        this.idCompeticao = null;
        this.codigo = null;
    }

    public String getIdCompeticao() { return this.idCompeticao; }
    public String getCodigo() { return this.codigo; }
}
