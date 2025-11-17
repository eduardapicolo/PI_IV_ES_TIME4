import br.com.salus.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class Competicao {
    MongoCollection<Document> colecaoCompeticoes;
    MongoCollection<Document> colecaoUsuarios;

    public Competicao(MongoDatabase db) {
        this.colecaoCompeticoes = db.getCollection("Competitions");
        this.colecaoUsuarios = db.getCollection("Users");
    }

    private String gerarCodigoUnico() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXXYZ0123456789";
        Random random = new Random();
        StringBuilder codigo = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(caracteres.length());
            codigo.append(caracteres.charAt(index));
        }

        return codigo.toString();
    }

    private Boolean codigoJaExiste(String codigo) {
        Document competicao = this.colecaoCompeticoes.find(
                Filters.eq("codigo", codigo)
        ).first();

        return competicao != null;
    }

    public Resposta criarCompeticao(PedidoDeNovaCompeticao pedido) {
        try {
            Document usuario = this.colecaoUsuarios.find(
                    Filters.eq("_id", new ObjectId(pedido.getIdCriador()))
            ).first();

            if (usuario == null) {
                return new Resposta(false, "Usuário não encontrado.");
            }

            String nomeUsuario = usuario.getString("apelido");

            String codigo;
            int tentativas = 0;
            do {
                codigo = gerarCodigoUnico();
                tentativas++;
                if (tentativas > 10) {
                    return new Resposta(false, "Erro ao gerar código único.");
                }
            } while (codigoJaExiste(codigo));

            Document criadorParticipante = new Document()
                    .append("idUsuario", pedido.getIdCriador())
                    .append("apelidoUsuario", nomeUsuario)
                    .append("checkins", new ArrayList<Document>())
                    .append("sequenciaAtual", 0);

            ArrayList<Document> participantes = new ArrayList<>();
            participantes.add(criadorParticipante);

            Document documentoCompeticao = new Document()
                    .append("nome", pedido.getNome())
                    .append("codigo", codigo)
                    .append("dataCriacao", new Date())
                    .append("idCriador", pedido.getIdCriador())
                    .append("participantes", participantes);

            InsertOneResult result = this.colecaoCompeticoes.insertOne(documentoCompeticao);

            if (result.getInsertedId() != null) {
                String idCompeticao = result.getInsertedId().asObjectId().getValue().toHexString();

                return new RespostaDeNovaCompeticao(
                        true,
                        "Competição criada com sucesso",
                        idCompeticao,
                        codigo
                );
            } else {
                return new Resposta(false, "Erro ao criar competição");
            }
        } catch (IllegalArgumentException e) {
            return new Resposta(false, "ID do usuário inválido.");
        } catch (Exception e) {
            System.err.println("Erro ao criar competição: " + e.getMessage());
            e.printStackTrace();
            return new Resposta(false, "Erro interno no servidor: " + e.getMessage());
        }
    }

    public Resposta entrarNaCompeticao(PedidoEntrarCompeticao pedido) {
        try {
            Document usuario = this.colecaoUsuarios.find(
                    Filters.eq("_id", new ObjectId(pedido.getIdUsuario()))
            ).first();

            if (usuario == null) {
                return new Resposta(false, "Usuário não encontrado.");
            }

            String nomeUsuario = usuario.getString("apelido");

            Document competicao = this.colecaoCompeticoes.find(
                    Filters.eq("codigo", pedido.getCodigo().toUpperCase())
            ).first();

            if (competicao == null) {
                return new Resposta(false, "Código inválido. Competição não encontrada.");
            }

            String idCompeticao = competicao.getObjectId("_id").toHexString();
            String nomeCompeticao = competicao.getString("nome");

            ArrayList<Document> participantes = (ArrayList<Document>) competicao.get("participantes");

            for (Document participante : participantes) {
                if (participante.getString("idUsuario").equals(pedido.getIdUsuario())) {
                    return new RespostaEntrarCompeticao(
                            false,
                            "Você já está participando desta competição.",
                            idCompeticao,
                            nomeCompeticao
                    );
                }
            }

            Document novoParticipante = new Document()
                    .append("idUsuario", pedido.getIdUsuario())
                    .append("apelidoUsuario", nomeUsuario)
                    .append("checkins", new ArrayList<Document>())
                    .append("sequenciaAtual", 0);

            participantes.add(novoParticipante);

            var updateResult = this.colecaoCompeticoes.updateOne(
                    Filters.eq("_id", competicao.getObjectId("_id")),
                    new Document("$set", new Document("participantes", participantes))
            );

            if (updateResult.getModifiedCount() > 0) {
                return new RespostaEntrarCompeticao(
                        true,
                        "Você entrou na competição!",
                        idCompeticao,
                        nomeCompeticao
                );
            } else {
                return new Resposta(false, "Erro ao entrar na competição.");
            }

        } catch (IllegalArgumentException e) {
            return new Resposta(false, "ID do usuário inválido.");
        } catch (Exception e) {
            e.printStackTrace();
            return new Resposta(false, "Erro interno no servidor: " + e.getMessage());
        }
    }

    public Resposta getCompeticoes (PedidoBuscaCompeticao pedido) {

        List<DocumentoCompeticao> listaCompeticoes = new ArrayList<>();

        try {
            String idUsuario = pedido.getIdUsuario().trim();

            Bson filtro = Filters.eq("participantes.idUsuario", idUsuario);
            MongoCursor<Document> cursor = this.colecaoCompeticoes.find(filtro).iterator();

            try (cursor) {
                while (cursor.hasNext()) {
                    Document doc = cursor.next();

                    List<DocumentoParticipante> listaParticipantes = new ArrayList<>();

                    List<Document> docsParticipantes = doc.getList("participantes",Document.class);

                    if (docsParticipantes != null) {
                        for (Document docP: docsParticipantes) {
                            String idUsarioP = docP.getString("idUsuario");
                            String apelidoP = docP.getString("apelidoUsuario");

                            DocumentoParticipante participante = new DocumentoParticipante(idUsarioP,apelidoP);
                            listaParticipantes.add(participante);
                        }
                    }

                    DocumentoCompeticao competicao = new DocumentoCompeticao(
                            doc.getObjectId("_id").toHexString(),
                            doc.getString("nome"),
                            doc.getString("codigo"),
                            doc.getString("idCriador"),
                            listaParticipantes

                    );

                    listaCompeticoes.add(competicao);
                }

                return new RespostaBuscaCompeticao(true, "Busca concluída com sucesso.",listaCompeticoes);
            }
        } catch (Exception e) {
            return new RespostaBuscaCompeticao(false, "ERRO NA BUSCA: " + e.getMessage(),null);
        }
    }
}
