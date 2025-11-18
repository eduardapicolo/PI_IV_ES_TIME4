package br.com.salus;

public class PedidoBuscaEmail extends Comunicado
{
    private static final long serialVersionUID = 5L;

    private String email;

    public PedidoBuscaEmail(String email) throws Exception {
        if (email == null) { throw new Exception("E-mail ausente."); }

        this.email = email;
    }

    public String getEmail() { return this.email; }
}
