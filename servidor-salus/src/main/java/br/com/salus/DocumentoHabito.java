package br.com.salus;

import java.io.Serializable;
import java.util.Date;

public class DocumentoHabito implements Serializable{
    private static final long serialVersionUID = 10L;

    private String id;
    private String nome;
    private Integer sequenciaCheckin;
    private Date ultimoCheckin;

    public DocumentoHabito(String id, String nome, Integer sequenciaCheckin, Date ultimoCheckin) {
        this.id = id;
        this.nome = nome;
        this.sequenciaCheckin = sequenciaCheckin;
        this.ultimoCheckin = ultimoCheckin;
    }

    public String getId() { return this.id; }
    public String getNome() { return this.nome; }
    public Integer getSequenciaCheckin() { return this.sequenciaCheckin; }
    public Date getUltimoCheckin() { return this.ultimoCheckin; }
}
