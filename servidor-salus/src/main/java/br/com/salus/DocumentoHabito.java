package br.com.salus;

import java.io.Serializable;
import java.util.Date;

public class DocumentoHabito implements Serializable{
    private static final long serialVersionUID = 10L;

    private String id;
    private String nome;
    private Integer sequenciaCheckin;
    private Date ultimoCheckin;
    private Integer idFotoPlanta;

    public DocumentoHabito(String id, String nome, Integer sequenciaCheckin, Date ultimoCheckin,  Integer idFotoPlanta) {
        this.id = id;
        this.nome = nome;
        this.sequenciaCheckin = sequenciaCheckin;
        this.ultimoCheckin = ultimoCheckin;
        this.idFotoPlanta = idFotoPlanta;
    }

    public String getId() { return this.id; }
    public String getNome() { return this.nome; }
    public Integer getSequenciaCheckin() { return this.sequenciaCheckin; }
    public Date getUltimoCheckin() { return this.ultimoCheckin; }
    public Integer getIdFotoPlanta() { return this.idFotoPlanta; }
}
