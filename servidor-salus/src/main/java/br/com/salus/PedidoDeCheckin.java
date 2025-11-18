package br.com.salus;

public class PedidoDeCheckin extends Comunicado {
    private static final long serialVersionUID = 11L;

    private String idHabito;

    public PedidoDeCheckin(
            String idHabito
    ) throws Exception
    {
        if (idHabito == null) {
            throw new Exception("Id do hábito não encontrado");
        }

        this.idHabito = idHabito;
    }

    public String getIdHabito() { return this.idHabito; }
}
