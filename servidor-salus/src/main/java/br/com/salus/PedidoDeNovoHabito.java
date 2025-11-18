package br.com.salus;

import java.util.Date;

public class PedidoDeNovoHabito extends Comunicado
{
    private static final long serialVersionUID = 7L;

    private String nome;
    private Integer sequenciaCheckin;
    private Date ultimoCheckin;
    private String userId;

    public PedidoDeNovoHabito(
            String nome,
            Integer sequenciaCheckin,
            Date ultimoCheckin,
            String userId
    ) throws Exception
    {
        if (nome == null) { throw new Exception("Nome ausente"); }

        this.nome = nome;

        this.sequenciaCheckin = sequenciaCheckin;

        this.ultimoCheckin = ultimoCheckin;

        if (userId == null) { throw new Exception("Id do usuario ausente"); }

        this.userId = userId;
    }

    public String getNome() { return nome; }

    public Integer getSequenciaCheckin() { return sequenciaCheckin; }

    public Date getUltimoCheckin() { return ultimoCheckin; }

    public String getUserId() { return userId; }

    @Override
    public String toString(){
        return "br.com.salus.PedidoDeNovoHabito [nome: " + this.nome + "\nuserId: " + this.userId + "]";
    }
}
