package br.com.salus;

public class PedidoDeCadastro extends Comunicado
{
    private static final long serialVersionUID = 2L;

    private String nome;
    private String email;
    private String senha;
    private String apelido;
    private Integer idFotoPerfil;
    public PedidoDeCadastro(
            String nome,
            String email,
            String senha,
            String apelido,
            Integer idFotoPerfil
    ) throws Exception
    {
        if (nome == null) { throw new Exception ("Nome ausente"); }

        this.nome = nome;

        if (email == null) { throw new Exception ("E-mail ausente"); }

        this.email = email;

        if (senha == null) { throw new Exception ("Senha ausente"); }

        this.senha = senha;

        if (apelido == null) { throw new Exception ("Apelido ausente"); }

        this.apelido = apelido;

        if (idFotoPerfil == null) { throw new Exception ("Foto de perfil ausente"); }

        this.idFotoPerfil = idFotoPerfil;
    }

    public String getNome (){
        return this.nome;
    }

    public String getEmail (){
        return this.email;
    }

    public String getSenha (){
        return this.senha;
    }

    public String getApelido (){
        return this.apelido;
    }

    public Integer getIdFotoPerfil (){
        return this.idFotoPerfil;
    }

    @Override
    public String toString (){
        return "br.com.gabriel.testecliente.PedidoDeCadastro [e-mail: " + this.email + "\nnome: " + this.nome;
    }


}
