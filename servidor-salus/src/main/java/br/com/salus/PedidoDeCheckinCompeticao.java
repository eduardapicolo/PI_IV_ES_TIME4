package br.com.salus;

import java.util.Date;

public class PedidoDeCheckinCompeticao extends Comunicado {
    private static final long serialVersionUID = 76L;

    private String idCompeticao;
    private String idUsuario;
    private Date dataCelularAtual;

    public PedidoDeCheckinCompeticao(
            String idCompeticao,
            String idUsuario,
            Date dataCelularAtual
    ) throws Exception
    {
        if (idCompeticao== null) {
            throw new Exception("Id da competição não encontrado");
        }
        if (idUsuario== null) {
            throw new Exception("Id do user não encontrado");
        }
        if (dataCelularAtual == null) {
            throw new Exception("Data atual não encontrada");
        }
        this.idCompeticao = idCompeticao;
        this.idUsuario = idUsuario;
        this.dataCelularAtual = dataCelularAtual;
    }

    public String getIdCompeticao() {
        return this.idCompeticao;
    }
    public String getIdUsuario() {
        return this.idUsuario;
    }
    public Date getDataCelularAtual() {
        return this.dataCelularAtual;
    }

}
