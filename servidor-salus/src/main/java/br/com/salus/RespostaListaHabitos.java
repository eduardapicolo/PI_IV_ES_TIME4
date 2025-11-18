package br.com.salus;

import java.util.ArrayList;
import java.util.List;

public class RespostaListaHabitos extends Resposta{
    private static final long serialVersionUID = 9L;

    private List<DocumentoHabito> habitos;

    public RespostaListaHabitos(boolean sucesso, String mensagem, List<DocumentoHabito> habitos) {
        super(sucesso, mensagem);
        this.habitos = habitos;
    }

    public RespostaListaHabitos(boolean sucesso, String mensagem) {
        super(sucesso, mensagem);
        this.habitos = null;
    }

    public List<DocumentoHabito> getHabitos() { return this.habitos; }
}
