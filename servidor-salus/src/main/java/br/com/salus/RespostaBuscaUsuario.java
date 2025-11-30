package br.com.salus;

public class RespostaBuscaUsuario extends Resposta {
    private static final long serialVersionUID = 501L;

    private DocumentoParticipante documentoParticipante;

    public RespostaBuscaUsuario(boolean sucesso, String mensagem, DocumentoParticipante documentoUsuario) {
        super(sucesso, mensagem);
        this.documentoParticipante = documentoUsuario;
    }

    public DocumentoParticipante getDocumentoUsuario() { return documentoParticipante; }
}
