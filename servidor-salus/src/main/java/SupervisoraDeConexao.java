import br.com.salus.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class SupervisoraDeConexao extends Thread
{
    private Parceiro            parceiro;
    private Socket              conexao;
    private ArrayList<Parceiro> usuarios;
    private Usuario             usuarioDAO;
    private Habito              habitoDAO;
    private Competicao          competicaoDAO;

    public SupervisoraDeConexao (Socket conexao, ArrayList<Parceiro> usuarios, Usuario usuarioDAO, Habito habitoDAO, Competicao competicaoDAO) throws Exception
    {
        if (conexao==null)
            throw new Exception ("Conexao ausente");

        if (usuarios==null)
            throw new Exception ("Usuarios ausentes");

        this.conexao  = conexao;
        this.usuarios = usuarios;
        this.usuarioDAO = usuarioDAO;
        this.habitoDAO = habitoDAO;
        this.competicaoDAO = competicaoDAO;
    }

    public void run ()
    {
        ObjectOutputStream transmissor;
        ObjectInputStream receptor;

        try
        {
            transmissor = new ObjectOutputStream(this.conexao.getOutputStream());
            transmissor.flush();
            receptor = new ObjectInputStream(this.conexao.getInputStream());
            this.parceiro = new Parceiro (this.conexao, receptor, transmissor);
        }
        catch (Exception erro)
        {
            System.err.println("Erro ao criar streams: " + erro.getMessage());
            return;
        }

        try
        {
            synchronized (this.usuarios)
            {
                this.usuarios.add (this.parceiro);
            }
        }
        catch (Exception e)
        {
            return;
        }

        System.out.println("Novo cliente conectado.");

        for(;;)
        {
            try
            {
                Comunicado comunicado = this.parceiro.envie();

                if (comunicado==null) {
                    break;
                }
                else if (comunicado instanceof PedidoBuscaEmail)
                {
                    PedidoBuscaEmail pedido = (PedidoBuscaEmail) comunicado;
                    Resposta resposta = this.usuarioDAO.buscarEmail(pedido);
                    this.parceiro.receba(resposta);
                }
                else if (comunicado instanceof PedidoDeCadastro)
                {
                    PedidoDeCadastro pedido = (PedidoDeCadastro) comunicado;
                    Resposta resposta = this.usuarioDAO.cadastrarUsuario(pedido);
                    this.parceiro.receba(resposta);
                }
                else if (comunicado instanceof PedidoDeLogin)
                {
                    PedidoDeLogin pedido = (PedidoDeLogin) comunicado;
                    Resposta resposta = this.usuarioDAO.loginUsuario(pedido);
                    this.parceiro.receba(resposta);
                }
                else if (comunicado instanceof PedidoDeNovoHabito)
                {
                    PedidoDeNovoHabito pedido = (PedidoDeNovoHabito) comunicado;
                    Resposta resposta = this.habitoDAO.cadastrarHabito(pedido);
                    this.parceiro.receba(resposta);
                }
                else if (comunicado instanceof PedidoListaHabitos)
                {
                    PedidoListaHabitos pedido = (PedidoListaHabitos) comunicado;
                    Resposta resposta = this.habitoDAO.buscarHabitos(pedido);
                    this.parceiro.receba(resposta);
                }
                else if (comunicado instanceof PedidoDeCheckin)
                {
                    PedidoDeCheckin pedido = (PedidoDeCheckin) comunicado;
                    Resposta resposta = this.habitoDAO.realizarCheckin(pedido);
                    this.parceiro.receba(resposta);
                }
                else if (comunicado instanceof PedidoDeNovaCompeticao)
                {
                    PedidoDeNovaCompeticao pedido = (PedidoDeNovaCompeticao) comunicado;
                    Resposta resposta = this.competicaoDAO.criarCompeticao(pedido);
                    this.parceiro.receba(resposta);
                }
                else if (comunicado instanceof PedidoEntrarCompeticao)
                {
                    PedidoEntrarCompeticao pedido = (PedidoEntrarCompeticao) comunicado;
                    Resposta resposta = this.competicaoDAO.entrarNaCompeticao(pedido);
                    this.parceiro.receba(resposta);
                }
                else if (comunicado instanceof PedidoBuscaCompeticao)
                {
                    PedidoBuscaCompeticao pedido = (PedidoBuscaCompeticao) comunicado;
                    Resposta resposta = this.competicaoDAO.getCompeticoes(pedido);
                    this.parceiro.receba(resposta);

                }
                else if (comunicado instanceof PedidoDeCheckinCompeticao){
                    PedidoDeCheckinCompeticao pedido = (PedidoDeCheckinCompeticao) comunicado;
                    Resposta resposta = this.competicaoDAO.realizarCheckinCompeticao(pedido);
                    this.parceiro.receba(resposta);
                }
                else if (comunicado instanceof PedidoExcluirHabito)
                {
                    PedidoExcluirHabito pedido = (PedidoExcluirHabito) comunicado;
                    Resposta resposta = this.habitoDAO.excluirHabito(pedido);
                    this.parceiro.receba(resposta);
                }
                else if (comunicado instanceof PedidoEdicaoConta)
                {
                    PedidoEdicaoConta pedido = (PedidoEdicaoConta) comunicado;
                    Resposta resposta = this.usuarioDAO.edicaoContaUsuario(pedido);
                    this.parceiro.receba(resposta);
                }
                else if (comunicado instanceof PedidoDeletarConta)
                {
                    PedidoDeletarConta pedido = (PedidoDeletarConta) comunicado;
                    Resposta resposta = this.usuarioDAO.deletarConta(pedido);
                    this.parceiro.receba(resposta);
                }
                else if (comunicado instanceof PedidoEdicaoHabito)
                {
                    PedidoEdicaoHabito pedido = (PedidoEdicaoHabito) comunicado;
                    Resposta resposta = this.habitoDAO.edicaoHabito(pedido);
                    this.parceiro.receba(resposta);
                }
                else if (comunicado instanceof PedidoEdicaoCompeticao)
                {
                    PedidoEdicaoCompeticao pedido = (PedidoEdicaoCompeticao) comunicado;
                    Resposta resposta = this.competicaoDAO.edicaoCompeticao(pedido);
                    this.parceiro.receba(resposta);
                }
                else if (comunicado instanceof PedidoBuscaUsuario)
                {
                    PedidoBuscaUsuario pedido = (PedidoBuscaUsuario) comunicado;
                    Resposta resposta = this.usuarioDAO.buscarUsuario(pedido);
                    this.parceiro.receba(resposta);
                }
                else if (comunicado instanceof PedidoParaSair)
                {
                    break;
                }
            }
            catch (Exception erro)
            {
                String mensagemErro = erro.getMessage() != null ? erro.getMessage().toLowerCase() : "";

                if (mensagemErro.contains("erro de recepcao") || mensagemErro.contains("connection reset")) {
                    System.err.println("Cliente encerrou a sessão de forma normal.");
                } else {
                    System.err.println("Erro fatal ou de processamento: " + erro.getMessage());
                    erro.printStackTrace();
                }

                break;
            }
        }

        synchronized (this.usuarios)
        {
            this.usuarios.remove (this.parceiro);
        }

        try {
            this.parceiro.adeus();
        } catch (Exception e) {}

        System.out.println("Cliente desconectado.");
    }
}
