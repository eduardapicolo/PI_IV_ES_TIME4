import br.com.salus.ComunicadoDeDesligamento;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.util.*;

public class Servidor
{
    public static String PORTA_PADRAO = "3000";

    public static void main (String[] args)
    {
        MongoClient mongoClient = null;
        MongoDatabase database = null;

        if (args.length>1)
        {
            System.err.println ("Uso esperado: java Servidor [PORTA]\n");
            return;
        }

        String porta=Servidor.PORTA_PADRAO;

        if (args.length==1)
            porta = args[0];

        try{
             mongoClient = MongoClients.create(MongoClientSettings.builder()//ALTERAR NO FUTURO PARA TER MAIS SEGURANÇA COM A CONNECTION STRING
                            .applyConnectionString(new ConnectionString("mongodb+srv://admin:salusPI04@salus.ztmuocz.mongodb.net/"))
                            .build());

            database = mongoClient.getDatabase("salus");
        }catch (Exception e){
            System.err.println("Erro ao conectar ao MongoDB: " + e.getMessage());
            return;
        }

        //DAO = Data Access Object, usuarioDAO serve para todos os fins que se comunicam com o banco de dados
        Usuario usuarioDAO = new Usuario(database);
        Habito habitoDAO = new Habito(database);
        Competicao competicaoDAO = new Competicao(database);

        ArrayList<Parceiro> usuarios = new ArrayList<Parceiro> ();

        AceitadoraDeConexao aceitadoraDeConexao = null;
        try
        {
            aceitadoraDeConexao = new AceitadoraDeConexao (porta, usuarios,usuarioDAO, habitoDAO, competicaoDAO);
            aceitadoraDeConexao.start();
        }
        catch (Exception erro)
        {
            System.err.println ("Escolha uma porta apropriada e liberada para uso!\n");
            return;
        }

        for(;;)
        {
            System.out.println ("O servidor esta ativo! Para desativa-lo,");
            System.out.println ("use o comando \"desativar\"\n");
            System.out.print   ("> ");

            String comando=null;
            try
            {
                comando = Teclado.getUmString();
            }
            catch (Exception erro)
            {}

            if (comando.toLowerCase().equals("desativar"))
            {
                synchronized (usuarios)
                {
                    ComunicadoDeDesligamento comunicadoDeDesligamento = new ComunicadoDeDesligamento();

                    for (Parceiro usuario:usuarios)
                    {
                        try
                        {
                            usuario.receba(comunicadoDeDesligamento);
                            usuario.adeus();
                        }
                        catch (Exception erro)
                        {}
                    }
                }

                System.out.println ("O servidor foi desativado!\n");
                System.exit(0);
            }
            else
                System.err.println ("Comando invalido!\n");
        }
    }
}
