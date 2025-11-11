import br.com.salus.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;

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

            Document documentoUsuario = new Document("nome",pedido.getNome())
                    .append("email",pedido.getEmail())
                    .append("senha",pedido.getSenha())
                    .append("apelido",pedido.getApelido())
                    .append("idFoto",pedido.getIdFotoPerfil())
                    .append("dataCadastro", pedido.getDataHoraCriacao());

            InsertOneResult result = this.colecaoUsuarios.insertOne(documentoUsuario);

            return new Resposta(true,"usuario cadastrado com sucesso");

        } catch (Exception e) {
            return new Resposta(false,"erro no interno no servidor " + e.getMessage() );
        }
    }

    public Resposta loginUsuario (PedidoDeLogin pedido) {
        System.out.println("\n========================================");
        System.out.println("=== DEBUG: loginUsuario ===");
        System.out.println("Email recebido: " + pedido.getEmail());
        System.out.println("Senha recebida: " + (pedido.getSenha() != null ? pedido.getSenha().substring(0, Math.min(3, pedido.getSenha().length())) + "***" : "null"));

        try {
            Document usuarioExistente = this.colecaoUsuarios.find(
                    Filters.and(
                            Filters.eq("email", pedido.getEmail()),
                            Filters.eq("senha", pedido.getSenha())
                    )
            ).first();

            if (usuarioExistente != null) {
                String userId = usuarioExistente.getObjectId("_id").toHexString();
                System.out.println("✅ Usuário encontrado! UserId: " + userId);
                System.out.println("Criando RespostaDeLogin com sucesso=true");

                RespostaDeLogin resposta = new RespostaDeLogin(true, "Login com sucesso", userId);

                System.out.println("RespostaDeLogin criada:");
                System.out.println("  - Sucesso: " + resposta.getSucesso());
                System.out.println("  - Mensagem: " + resposta.getMensagem());
                System.out.println("  - UserId: " + resposta.getUserId());
                System.out.println("========================================\n");

                return resposta;
            }

            System.out.println("❌ Usuário NÃO encontrado no banco");
            System.out.println("Criando RespostaDeLogin com sucesso=false");

            RespostaDeLogin respostaFalha = new RespostaDeLogin(false, "E-mail ou senha incorreto.");

            System.out.println("RespostaDeLogin criada:");
            System.out.println("  - Sucesso: " + respostaFalha.getSucesso());
            System.out.println("  - Mensagem: " + respostaFalha.getMensagem());
            System.out.println("========================================\n");

            return respostaFalha;

        } catch (Exception e) {
            System.err.println("❌ EXCEÇÃO no loginUsuario: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================\n");
            return new Resposta(false,"Erro interno no servidor " + e.getMessage());
        }
    }
}
