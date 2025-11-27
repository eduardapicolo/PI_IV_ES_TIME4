import br.com.salus.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt;

public class Usuario {
    MongoCollection<Document> colecaoUsuarios;

    public Usuario (MongoDatabase db)
    {
        this.colecaoUsuarios = db.getCollection("Users");
    }

    public Resposta buscarEmail (PedidoBuscaEmail pedido) {
        try {
            Document emailExistente = this.colecaoUsuarios.find(
                    Filters.eq("email", pedido.getEmail())
            ).first();

            if (emailExistente != null) {
                return new Resposta(true, "Email encontrado");
            }

            return new Resposta(false, "Nenhum email encontrado");
        } catch (Exception e) {
            return new Resposta(false, "Erro interno no servido: " + e.getMessage());
        }
    }

    public Resposta cadastrarUsuario (PedidoDeCadastro pedido) {

        try {
            PedidoBuscaEmail pedidoBuscaEmail = new PedidoBuscaEmail(pedido.getEmail());
            Resposta emailExistente = buscarEmail(pedidoBuscaEmail);

            if (emailExistente.getSucesso()) {
                return new Resposta(false,"E-mail ja existente");
            }

            Document apelidoExistente = this.colecaoUsuarios.find(
                    Filters.eq("apelido",pedido.getApelido())
            ).first();

            if (apelidoExistente != null) {
                return new Resposta(false,"Apelido ja existente");
            }

            String senhaHash = BCrypt.hashpw(pedido.getSenha(), BCrypt.gensalt());

            Document documentoUsuario = new Document("nome",pedido.getNome())
                    .append("email",pedido.getEmail())
                    .append("senha",senhaHash)
                    .append("apelido",pedido.getApelido())
                    .append("idFoto",pedido.getIdFotoPerfil())
                    .append("dataCadastro", pedido.getDataHoraCriacao());

            InsertOneResult result = this.colecaoUsuarios.insertOne(documentoUsuario);

            Document usuarioExistente = this.colecaoUsuarios.find(
                    Filters.eq("email", pedido.getEmail())
            ).first();

            if (usuarioExistente != null) {
                String userId = usuarioExistente.getObjectId("_id").toHexString();
                return new RespostaPedidoDeCadastro(true,"usuario cadastrado com sucesso",userId);
            }

            return new RespostaPedidoDeCadastro(false, "Não foi possivel criar uma conta");


        } catch (Exception e) {
            return new Resposta(false,"erro no interno no servidor " + e.getMessage() );
        }
    }

    public Resposta loginUsuario (PedidoDeLogin pedido) {
        try {
            Document usuarioExistente = this.colecaoUsuarios.find(
                    Filters.eq("email", pedido.getEmail())
            ).first();

            if (usuarioExistente != null) {
                String senhaArmazenadaHash = usuarioExistente.getString("senha");

                if (BCrypt.checkpw(pedido.getSenha(), senhaArmazenadaHash)) {
                    String userId = usuarioExistente.getObjectId("_id").toHexString();
                    return new RespostaDeLogin(true, "Login com sucesso", userId);
                }
            }

            return new RespostaDeLogin(false, "E-mail ou senha incorreto");
        } catch (Exception e) {
            System.err.println("EXCEÇÃO no loginUsuario: " + e.getMessage());
            e.printStackTrace();
            return new Resposta(false,"Erro interno no servidor " + e.getMessage());
        }
    }
}
