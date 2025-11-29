package br.com.salus;

import java.io.Serializable;
import java.util.List;

public class DocumentoCompeticao implements Serializable {
    private static final long serialVersionUID = 21L;

    private String id;
    private String nome;
    private String codigo;
    private String idCriador;
    private List<DocumentoParticipante> participantes;
    private int idIcone;

    public DocumentoCompeticao(String id, String nome, String codigo, String idCriador, List<DocumentoParticipante> participantes, int idIcone ) {
        this.id = id;
        this.nome = nome;
        this.codigo = codigo;
        this.idCriador = idCriador;
        this.participantes = participantes;
        this.idIcone = idIcone;
    }

    public String getId() { return this.id; }
    public String getNome() { return this.nome; }
    public String getCodigo() { return this.codigo; }
    public String getIdCriador() { return this.idCriador; }
    public List<DocumentoParticipante> getParticipantes() { return this.participantes; }
    public int getIdIcone() { return this.idIcone; }
}
