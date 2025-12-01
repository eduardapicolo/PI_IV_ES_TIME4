package br.com.salus;

import java.util.List;
import org.bson.Document;

public class RespostaBuscaCompeticao extends Resposta {
    private static final long serialVersionUID = 19L;
    private List<DocumentoCompeticao> competicoes;

    public RespostaBuscaCompeticao(boolean sucesso, String mensagem, List<DocumentoCompeticao> competicoes) {
        super(sucesso, mensagem);
        this.competicoes = competicoes;
    }

    public List<DocumentoCompeticao> getCompeticoes() {
        return competicoes;
    }
}