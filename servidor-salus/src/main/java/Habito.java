import br.com.salus.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Habito {
    MongoCollection<Document> colecaoUsuarios;
    MongoCollection<Document> colecaoHabitos;

    public Habito (MongoDatabase db) {
        this.colecaoUsuarios = db.getCollection("Users");
        this.colecaoHabitos = db.getCollection("Habits");
    }

    public Resposta cadastrarHabito (PedidoDeNovoHabito pedido) {
        try {
            Document habitoExistente = this.colecaoHabitos.find(
                    Filters.and(
                            Filters.eq("nome", pedido.getNome()),
                            Filters.eq("idUsuario", pedido.getUserId().trim())
                    )
            ).first();

            if (habitoExistente != null) {
                return new Resposta(false, "Habito ja criado. Utilize outro nome.");
            }

            Document documentoHabito = new Document("nome", pedido.getNome())
                    .append("sequenciaCheckin", pedido.getSequenciaCheckin())
                    .append("ultimoCheckin", pedido.getUltimoCheckin())
                    .append("idUsuario", pedido.getUserId().toString())
                    .append("idFotoPlanta", pedido.getIdFotoPlanta());

            InsertOneResult result = this.colecaoHabitos.insertOne(documentoHabito);

            return new Resposta(true, "Habito cadastrado.");
        } catch (Exception e) {
            return new Resposta(false,"erro no interno no servidor " + e.getMessage() );
        }
    }

    public Resposta buscarHabitos(PedidoListaHabitos pedido) {
        try {
            String idDoUsuarioString = pedido.getUserId().trim();
            List<DocumentoHabito> habitosEncontrados = new ArrayList<>();

            var filtro = Filters.eq("idUsuario", idDoUsuarioString);

            MongoCursor<Document> cursor = this.colecaoHabitos.find(filtro).iterator();

            try {
                while (cursor.hasNext()) {
                    Document doc = cursor.next();
                    try {
                        Number seqNum = doc.get("sequenciaCheckin", Number.class);
                        Integer sequencia = (seqNum != null) ? seqNum.intValue() : 0;
                        Integer idFotoPlanta = doc.getInteger("idFotoPlanta", 1);

                        DocumentoHabito dto = new DocumentoHabito(
                                doc.getObjectId("_id").toHexString(),
                                doc.getString("nome"),
                                sequencia,
                                doc.getDate("ultimoCheckin"),
                                idFotoPlanta
                        );
                        habitosEncontrados.add(dto);
                    } catch (Exception e_dto) {

                    }
                }
            } finally {
                cursor.close();
            }

            return new RespostaListaHabitos(true, "Busca concluída.", habitosEncontrados);

        } catch (Exception e) {
            return new RespostaListaHabitos(false, "Erro interno no servidor: " + e.getMessage());
        }
    }

    public Resposta realizarCheckin(PedidoDeCheckin pedido) {

        System.out.println("\n======================================");
        System.out.println("--- DEBUG: realizarCheckin ---");

        try {
            ObjectId idDoHabito;
            try {
                idDoHabito = new ObjectId(pedido.getIdHabito());
                System.out.println("ID do Hábito recebido: " + idDoHabito.toHexString());
            } catch (IllegalArgumentException erro) {
                return new RespostaDeCheckin(false, "ID do hábito em formato inválido.");
            }

            // 1. Buscar o hábito
            Document habito = this.colecaoHabitos.find(
                    Filters.eq("_id", idDoHabito)
            ).first();

            if (habito == null) {
                System.out.println("ERRO: Hábito com _id " + idDoHabito + " não foi encontrado.");
                return new RespostaDeCheckin(false, "Hábito não encontrado.");
            }

            System.out.println("...Hábito encontrado. JSON: " + habito.toJson());

            // 2. Pegar os dados atuais
            Date ultimoCheckinDate = habito.getDate("ultimoCheckin");
            System.out.println("Valor lido de 'ultimoCheckin' (java.util.Date): " + ultimoCheckinDate);

            // Vamos usar a leitura robusta de 'Number'
            Number seqNum = habito.get("sequenciaCheckin", Number.class);
            int sequenciaAtual = (seqNum != null) ? seqNum.intValue() : 0;
            System.out.println("Sequência atual lida: " + sequenciaAtual);

            Integer idFotoPlanta = habito.getInteger("idFotoPlanta", 1);

            // 3. Lógica da Sequência (Streak)
            LocalDate hoje = LocalDate.now(ZoneId.systemDefault());
            System.out.println("'Hoje' (LocalDate): " + hoje);
            int novaSequencia = sequenciaAtual;

            if (ultimoCheckinDate == null) {
                System.out.println("Caminho A: ultimoCheckinDate é NULL. Primeiro check-in.");
                novaSequencia = 1;
            } else {
                System.out.println("Caminho B: ultimoCheckinDate NÃO é null.");
                LocalDate ultimoCheckin = ultimoCheckinDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                System.out.println("'ultimoCheckin' (LocalDate): " + ultimoCheckin);

                if (ultimoCheckin.isEqual(hoje)) {
                    System.out.println("Caminho B1: Check-in já realizado hoje. Retornando erro.");
                    return new RespostaDeCheckin(false, "Check-in já realizado hoje.");

                } else if (ultimoCheckin.isEqual(hoje.minusDays(1))) {
                    System.out.println("Caminho B2: Check-in foi ontem. Continuando sequência.");
                    novaSequencia++;
                } else {
                    System.out.println("Caminho B3: Sequência quebrada. Reiniciando.");
                    novaSequencia = 1;
                }
            }

            System.out.println("Nova sequência calculada: " + novaSequencia);

            // 4. Atualizar o hábito no banco
            Date dataDoCheckin = new Date();
            System.out.println("Salvando 'ultimoCheckin' como (java.util.Date): " + dataDoCheckin);

            UpdateResult updateResult = this.colecaoHabitos.updateOne(
                    Filters.eq("_id", idDoHabito),
                    Updates.combine(
                            Updates.set("sequenciaCheckin", novaSequencia),
                            Updates.set("ultimoCheckin", dataDoCheckin)
                    )
            );

            if (updateResult.getModifiedCount() > 0) {
                System.out.println("... Update no banco foi BEM-SUCEDIDO.");
                DocumentoHabito habitoAtualizadoDto = new DocumentoHabito(
                        idDoHabito.toHexString(),
                        habito.getString("nome"),
                        novaSequencia,
                        dataDoCheckin,
                        idFotoPlanta
                );
                return new RespostaDeCheckin(true, "Check-in realizado!", habitoAtualizadoDto);
            } else {
                System.out.println("... ERRO: updateResult.getModifiedCount() foi 0.");
                return new RespostaDeCheckin(false, "Não foi possível atualizar o check-in.");
            }

        } catch (Exception e) {
            System.err.println("--- ERRO GERAL no realizarCheckin ---");
            e.printStackTrace();
            return new RespostaDeCheckin(false, "Erro interno no servidor: " + e.getMessage());
        }
    }

    public Resposta excluirHabito(PedidoExcluirHabito pedido) {
        try {
            ObjectId objectIdHabito;
            try {
                objectIdHabito = new ObjectId(pedido.getIdHabito());
            } catch (IllegalArgumentException e) {
                return new Resposta(false, "ID do hábito inválido.");
            }

            var filtro = Filters.and(
                    Filters.eq("_id", objectIdHabito),
                    Filters.eq("idUsuario", pedido.getIdUsuario().trim())
            );

            DeleteResult resultado = this.colecaoHabitos.deleteOne(filtro);

            if (resultado.getDeletedCount() > 0) {
                return new Resposta(true, "Hábito excluído com sucesso.");
            } else {
                return new Resposta(false, "Hábito não encontrado ou você não tem permissão para excluí-lo.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new Resposta(false, "Erro interno ao excluir hábito: " + e.getMessage());
        }
    }
}
