package br.com.salus;

import java.util.List;
import org.bson.Document;

public class RespostaBuscaCompeticao extends Resposta {
    private static final long serialVersionUID = 19L;
    private List<Document> competicoes;

    public RespostaBuscaCompeticao(boolean sucesso, String mensagem, List<Document> competicoes) {
        super(sucesso, mensagem);
        this.competicoes = competicoes;
    }

    public List<Document> getCompeticoes() {
        return competicoes;
    }
}