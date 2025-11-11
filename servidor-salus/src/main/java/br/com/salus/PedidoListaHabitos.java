package br.com.salus;

import java.sql.SQLException;

public class PedidoListaHabitos extends Comunicado{
    private static final long serialVersionUID = 8L;

    private String userId;

    public PedidoListaHabitos(String userId) throws Exception {
        if (userId == null || userId.isEmpty()){
            throw new Exception("ID de usuário ausente.");
        }
        this.userId = userId;
    }

    public String getUserId() { return this.userId; }
}
