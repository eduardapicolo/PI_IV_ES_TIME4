package br.com.salus;

import java.io.Serializable;
import java.util.Date;

public class DocumentoParticipante implements Serializable {
    private static final long serialVersionUID = 20L;

    private String idUsuario;
    private String apelidoUsuario;
    private Date ultimoCheckin;
    private Integer sequencia;
    private Integer idFotoPerfil;

    public DocumentoParticipante(String id, String apelidoUsuario, Date ultimoCheckin, Integer sequencia, Integer idFotoPerfil) {
        this.idUsuario = id;
        this.apelidoUsuario = apelidoUsuario;
        this.ultimoCheckin = ultimoCheckin;
        this.sequencia = sequencia;
        this.idFotoPerfil = idFotoPerfil;
    }

    public String getIdUsuario() { return this.idUsuario; }
    public String getApelidoUsuario() { return this.apelidoUsuario; }
    public Date getUltimoCheckin() { return this.ultimoCheckin; }
    public Integer getSequencia() { return this.sequencia; }
    public Integer getIdFotoPerfil() { return this.idFotoPerfil; }
}
