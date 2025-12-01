package br.com.salus;

public class RespostaDeCheckin extends Resposta {
    private static final long serialVersionUID = 12L;
    private DocumentoHabito habitoAtualizado;

    public RespostaDeCheckin(boolean sucesso, String msg, DocumentoHabito habitoAtualizado) {
        super(sucesso, msg);
        this.habitoAtualizado = habitoAtualizado;
    }

    public RespostaDeCheckin(boolean sucesso, String msg) {
        super(sucesso, msg);
        this.habitoAtualizado = null;
    }

    public DocumentoHabito getHabitoAtualizado() { return this.habitoAtualizado; }
}
