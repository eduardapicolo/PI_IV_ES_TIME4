package br.com.salus;

public class PedidoDeLogin extends Comunicado
{
    private static final long serialVersionUID = 6L;

    private String email;
    private String senha;

    public PedidoDeLogin(String email, String senha) throws Exception {
        if (email == null || email.isEmpty()) { throw new Exception("E-mail inválido."); }

        this.email = email;

        if (senha == null) { throw new Exception("Senha inválida."); }

        this.senha = senha;
    }

    public String getEmail() { return this.email; }

    public String getSenha() { return this.senha; }

    @Override
    public String toString (){
        return "br.com.salus.PedidoDeLogin [e-mail: " + this.email + "]";
    }
}
