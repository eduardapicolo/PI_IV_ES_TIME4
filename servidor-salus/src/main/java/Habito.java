import br.com.salus.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.bson.types.ObjectId;

public class Habito {
    MongoCollection<Document> colecaoUsuarios;
    MongoCollection<Document> colecaoHabitos;

    public Habito (MongoDatabase db) {
        this.colecaoUsuarios = db.getCollection("Users");
        this.colecaoHabitos = db.getCollection("Habits");
    }

    public Resposta cadastrarHabito (PedidoDeNovoHabito pedido) {
        try {
            ObjectId idDoUsuario;

            try {
                idDoUsuario = new ObjectId(pedido.getUserId());
            } catch (IllegalArgumentException erro) {
                return new Resposta(false, "ID de usuario em formato invalido.");
            }

            Document usuario = this.colecaoUsuarios.find(
                    Filters.eq("_id", idDoUsuario)
            ).first();
            if (usuario == null) {
                return new Resposta(false, "Usuário dono do hábito não encontrado");
            }

            Document habitoExistente = this.colecaoHabitos.find(
                    Filters.and(
                            Filters.eq("nome", pedido.getNome()),
                            Filters.eq("idUsuario", idDoUsuario)
                    )
            ).first();

            if (habitoExistente != null) {
                return new Resposta(false, "Habito ja criado. Utilize outro nome.");
            }

            Document documentoHabito = new Document("nome", pedido.getNome())
                    .append("sequenciaCheckin", pedido.getSequenciaCheckin())
                    .append("ultimoCheckin", pedido.getUltimoCheckin())
                    .append("idUsuario", idDoUsuario);

            InsertOneResult result = this.colecaoHabitos.insertOne(documentoHabito);

            return new Resposta(true, "Habito cadastrado.");
        } catch (Exception e) {
            return new Resposta(false,"erro no interno no servidor " + e.getMessage() );
        }
    }
}
