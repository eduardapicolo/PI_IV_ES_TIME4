import br.com.salus.Comunicado;
import br.com.salus.PedidoDeCadastro;
import br.com.salus.PedidoParaSair;
import br.com.salus.Resposta;

import java.io.*;
import java.net.*;
import java.util.*;

public class SupervisoraDeConexao extends Thread
{
    private Parceiro            parceiro;
    private Socket              conexao;
    private ArrayList<Parceiro> usuarios;
    private Usuario       usuarioDAO;

    public SupervisoraDeConexao (Socket conexao, ArrayList<Parceiro> usuarios, Usuario usuarioDAO) throws Exception
    {
        if (conexao==null)
            throw new Exception ("Conexao ausente");

        if (usuarios==null)
            throw new Exception ("Usuarios ausentes");

        this.conexao  = conexao;
        this.usuarios = usuarios;
        this.usuarioDAO = usuarioDAO;
    }

    public void run ()
    {
        ObjectOutputStream transmissor;
        try
        {
            transmissor = new ObjectOutputStream(this.conexao.getOutputStream());
        }
        catch (Exception erro)
        {
            return;
        }

        ObjectInputStream receptor=null;
        try
        {
            receptor = new ObjectInputStream(this.conexao.getInputStream());
        }
        catch (Exception err0)
        {
            try
            {
                transmissor.close();
            }
            catch (Exception falha)
            {} // so tentando fechar antes de acabar a thread

            return;
        }

        try
        {
            this.parceiro = new Parceiro (this.conexao,receptor,transmissor);
        }
        catch (Exception erro)
        {} // sei que passei os parametros corretos

        try
        {
            synchronized (this.usuarios)
            {
                this.usuarios.add (this.parceiro);
            }


            for(;;)
            {
                Comunicado comunicado = this.parceiro.envie();

                if (comunicado==null) {
                    return;
                }
                else if (comunicado instanceof PedidoDeCadastro)
                {
                    PedidoDeCadastro pedido = (PedidoDeCadastro) comunicado;
                    Resposta resposta = this.usuarioDAO.cadastrarUsuario(pedido);
                    this.parceiro.receba(resposta);

                }
                else if (comunicado instanceof PedidoParaSair)
                {
                    synchronized (this.usuarios)
                    {
                        this.usuarios.remove (this.parceiro);
                    }
                    this.parceiro.adeus();
                }
            }
        }
        catch (Exception erro)
        {
            try
            {
                transmissor.close ();
                receptor   .close ();
            }
            catch (Exception falha)
            {} // so tentando fechar antes de acabar a thread

            return;
        }
    }
}
