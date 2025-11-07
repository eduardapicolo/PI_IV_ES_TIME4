import com.mongodb.client.MongoDatabase;

import java.net.*;
import java.util.*;

public class AceitadoraDeConexao extends Thread
{
    private ServerSocket        pedido;
    private ArrayList<Parceiro> usuarios;
    private Usuario             usuarioDAO;
    private Habito              habitoDAO;

    public AceitadoraDeConexao (String porta, ArrayList<Parceiro> usuarios,Usuario usuarioDAO, Habito habitoDAO) throws Exception
    {
        if (porta==null)
            throw new Exception ("Porta ausente");

        try
        {
            this.pedido = new ServerSocket (Integer.parseInt(porta));
        }
        catch (Exception  erro)
        {
            throw new Exception ("Porta invalida");
        }

        if (usuarios==null)
            throw new Exception ("Usuarios ausentes");

        this.usuarios = usuarios;

        this.usuarioDAO = usuarioDAO;
        this.habitoDAO = habitoDAO;
    }

    public void run ()
    {
        for(;;)
        {
            Socket conexao=null;
            try
            {
                conexao = this.pedido.accept();
            }
            catch (Exception erro)
            {
                continue;
            }

            SupervisoraDeConexao supervisoraDeConexao = null;
            try
            {
                supervisoraDeConexao = new SupervisoraDeConexao (conexao, usuarios,this.usuarioDAO, this.habitoDAO);
            }
            catch (Exception erro)
            {} // sei que passei parametros corretos para o construtor
            supervisoraDeConexao.start();
        }
    }
}
