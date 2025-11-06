import br.com.salus.PedidoDeCadastro;
import br.com.salus.Resposta;
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

    public Resposta cadastrarUsuario (PedidoDeCadastro pedido) {

        try {
            Document emailExistente = this.colecaoUsuarios.find(
                    Filters.eq("email",pedido.getEmail())
            ).first();

            if (emailExistente != null) {
                return new Resposta(false,"e-mail ja existente");
            }

            Document apelidoExistente = this.colecaoUsuarios.find(
                    Filters.eq("apelido",pedido.getApelido())
            ).first();

            if (apelidoExistente != null) {
                return new Resposta(false,"apelido ja esta sendo utilizado por outro usuario");
            }

            Document documentoUsuario = new Document("nome",pedido.getNome())
                    .append("email",pedido.getEmail())
                    .append("senha",pedido.getSenha())
                    .append("apelido",pedido.getApelido())
                    .append("idFoto",pedido.getIdFotoPerfil());

            InsertOneResult result = this.colecaoUsuarios.insertOne(documentoUsuario);

            return new Resposta(true,"usuario cadastrado com sucesso");

        } catch (Exception e) {
            return new Resposta(false,"erro no interno no servidor " + e.getMessage() );
        }

    }


}
