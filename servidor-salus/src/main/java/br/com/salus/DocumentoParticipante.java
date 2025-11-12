package br.com.salus;

import java.io.Serializable;

public class DocumentoParticipante implements Serializable {
    private static final long serialVersionUID = 20L;

    private String idUsuario;
    private String apelidoUsuario;

    public DocumentoParticipante(String id, String apelidoUsuario) {
        this.idUsuario = id;
        this.apelidoUsuario = apelidoUsuario;
    }

    public String getIdUsuario() { return this.idUsuario; }
    public String getApelidoUsuario() { return this.apelidoUsuario; }
}
