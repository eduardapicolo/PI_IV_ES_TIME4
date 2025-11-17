package br.com.salus;

public class RespostaDeCheckinCompeticao extends Resposta{
    private static final long serialVersionUID = 77L;
    private DocumentoCompeticao competicaoAtualizada;

    public RespostaDeCheckinCompeticao(boolean sucesso, String msg, DocumentoCompeticao competicaoAtualizada) {
        super(sucesso, msg);
        this.competicaoAtualizada = competicaoAtualizada;
    }

    public RespostaDeCheckinCompeticao(boolean sucesso, String msg) {
        super(sucesso, msg);
        this.competicaoAtualizada = null;
    }

    public DocumentoCompeticao getCompeticaoAtualizada() {
        return this.competicaoAtualizada;
    }
}
