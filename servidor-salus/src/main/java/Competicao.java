import br.com.salus.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.*;

public class Competicao {
    MongoCollection<Document> colecaoCompeticoes;
    MongoCollection<Document> colecaoUsuarios;

    public Competicao(MongoDatabase db) {
        this.colecaoCompeticoes = db.getCollection("Competitions");
        this.colecaoUsuarios = db.getCollection("Users");
    }

    private String gerarCodigoUnico() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
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
            Integer idFotoUsuario = usuario.getInteger("idFoto", 1);

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
                    .append("idFotoPerfil", idFotoUsuario)
                    .append("ultimoCheckin", null)
                    .append("sequenciaAtual", 0);

            ArrayList<Document> participantes = new ArrayList<>();
            participantes.add(criadorParticipante);

            Document documentoCompeticao = new Document()
                    .append("nome", pedido.getNome())
                    .append("codigo", codigo)
                    .append("dataCriacao", new Date())
                    .append("idCriador", pedido.getIdCriador())
                    .append("idIcone", pedido.getIdIcone())
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
            System.out.println("=== ENTRANDO NA COMPETIÇÃO ===");
            System.out.println("Código recebido: '" + pedido.getCodigo() + "'");
            System.out.println("ID do usuário: " + pedido.getIdUsuario());

            Document usuario = this.colecaoUsuarios.find(
                    Filters.eq("_id", new ObjectId(pedido.getIdUsuario()))
            ).first();

            if (usuario == null) {
                System.out.println("❌ Usuário não encontrado");
                return new Resposta(false, "Usuário não encontrado.");
            }

            String nomeUsuario = usuario.getString("apelido");
            Integer idFotoUsuario = usuario.getInteger("idFoto", 1);
            System.out.println("✅ Usuário encontrado: " + nomeUsuario + " (Foto ID: " + idFotoUsuario + ")");

            String codigoBusca = pedido.getCodigo().trim().toUpperCase();
            System.out.println("Buscando competição com código: '" + codigoBusca + "'");

            Document competicao = this.colecaoCompeticoes.find(
                    Filters.eq("codigo", codigoBusca)
            ).first();

            if (competicao == null) {
                System.out.println("❌ Competição não encontrada com código: " + codigoBusca);
                return new Resposta(false, "Código inválido. Competição não encontrada.");
            }

            System.out.println("✅ Competição encontrada: " + competicao.getString("nome"));

            String idCompeticao = competicao.getObjectId("_id").toHexString();
            String nomeCompeticao = competicao.getString("nome");

            ArrayList<Document> participantes = (ArrayList<Document>) competicao.get("participantes");

            for (Document participante : participantes) {
                if (participante.getString("idUsuario").equals(pedido.getIdUsuario())) {
                    System.out.println("⚠️ Usuário já está na competição");
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
                    .append("idFotoPerfil", idFotoUsuario)
                    .append("ultimoCheckin", null)
                    .append("sequenciaAtual", 0);

            participantes.add(novoParticipante);

            var updateResult = this.colecaoCompeticoes.updateOne(
                    Filters.eq("_id", competicao.getObjectId("_id")),
                    new Document("$set", new Document("participantes", participantes))
            );

            if (updateResult.getModifiedCount() > 0) {
                System.out.println("✅ Usuário adicionado à competição com sucesso!");
                return new RespostaEntrarCompeticao(
                        true,
                        "Você entrou na competição!",
                        idCompeticao,
                        nomeCompeticao
                );
            } else {
                System.out.println("❌ Erro ao atualizar competição");
                return new Resposta(false, "Erro ao entrar na competição.");
            }

        } catch (IllegalArgumentException e) {
            System.out.println("❌ ID do usuário inválido: " + e.getMessage());
            return new Resposta(false, "ID do usuário inválido.");
        } catch (Exception e) {
            System.err.println("❌ Exceção ao entrar na competição:");
            e.printStackTrace();
            return new Resposta(false, "Erro interno no servidor: " + e.getMessage());
        }
    }

    public Resposta getCompeticoes(PedidoBuscaCompeticao pedido) {
        List<DocumentoCompeticao> listaCompeticoes = new ArrayList<>();

        try {
            String idUsuario = pedido.getIdUsuario().trim();

            Bson filtro = Filters.eq("participantes.idUsuario", idUsuario);
            MongoCursor<Document> cursor = this.colecaoCompeticoes.find(filtro).iterator();

            try (cursor) {
                while (cursor.hasNext()) {
                    Document doc = cursor.next();

                    List<DocumentoParticipante> listaParticipantes = new ArrayList<>();
                    List<Document> docsParticipantes = doc.getList("participantes", Document.class);

                    if (docsParticipantes != null) {
                        for (Document docP : docsParticipantes) {
                            String idUsuarioP = docP.getString("idUsuario");
                            String apelidoP = docP.getString("apelidoUsuario");
                            Date ultimoCheckinP = docP.getDate("ultimoCheckin");
                            Integer sequenciaP = docP.getInteger("sequenciaAtual");

                            Integer idFotoPerfilP = 1;

                            try {
                                Document usuarioAtual = this.colecaoUsuarios.find(
                                        Filters.eq("_id", new ObjectId(idUsuarioP))
                                ).first();

                                if (usuarioAtual != null) {
                                    idFotoPerfilP = usuarioAtual.getInteger("idFoto", 1);
                                    System.out.println("📸 Usuário: " + apelidoP + " | ID Foto: " + idFotoPerfilP);
                                } else {
                                    System.out.println("⚠️ Usuário não encontrado: " + idUsuarioP);
                                    idFotoPerfilP = docP.getInteger("idFotoPerfil", 1);
                                }
                            } catch (IllegalArgumentException e) {
                                System.err.println("⚠️ ID inválido para usuário " + apelidoP + ": " + e.getMessage());
                                idFotoPerfilP = docP.getInteger("idFotoPerfil", 1);
                            } catch (Exception e) {
                                System.err.println("⚠️ Erro ao buscar foto do usuário " + apelidoP + ": " + e.getMessage());
                                idFotoPerfilP = docP.getInteger("idFotoPerfil", 1);
                            }

                            DocumentoParticipante participante = new DocumentoParticipante(
                                    idUsuarioP,
                                    apelidoP,
                                    ultimoCheckinP,
                                    sequenciaP,
                                    idFotoPerfilP
                            );
                            listaParticipantes.add(participante);
                        }
                    }

                    Integer idIcone = doc.getInteger("idIcone", 1);

                    DocumentoCompeticao competicao = new DocumentoCompeticao(
                            doc.getObjectId("_id").toHexString(),
                            doc.getString("nome"),
                            doc.getString("codigo"),
                            doc.getString("idCriador"),
                            listaParticipantes,
                            idIcone
                    );

                    listaCompeticoes.add(competicao);
                }

                System.out.println("✅ Total de competições encontradas: " + listaCompeticoes.size());
                return new RespostaBuscaCompeticao(true, "Busca concluída com sucesso.", listaCompeticoes);
            }
        } catch (Exception e) {
            System.err.println("❌ Erro na busca de competições: " + e.getMessage());
            e.printStackTrace();
            return new RespostaBuscaCompeticao(false, "ERRO NA BUSCA: " + e.getMessage(), null);
        }
    }

    private boolean isMesmoDia(Date data1, Date data2) {
        if (data1 == null || data2 == null) {
            return false;
        }

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(data1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(data2);

        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    public Resposta realizarCheckinCompeticao(PedidoDeCheckinCompeticao pedido) {
        try {
            ObjectId idDaCompeticao;
            try {
                idDaCompeticao = new ObjectId(pedido.getIdCompeticao());
            } catch (IllegalArgumentException erro) {
                return new RespostaDeCheckinCompeticao(false, "ID da competição em formato inválido.");
            }

            Document competicao = this.colecaoCompeticoes.find(Filters.eq("_id", idDaCompeticao)).first();

            if (competicao == null) {
                return new RespostaDeCheckinCompeticao(false, "Competição não encontrada.");
            }

            ArrayList<Document> participantes = (ArrayList<Document>) competicao.getList("participantes", Document.class);
            if (participantes == null || participantes.isEmpty()) {
                return new RespostaDeCheckinCompeticao(false, "Erro: Competição sem participantes registrados.");
            }

            for (Document participante : participantes) {
                if (participante.getString("idUsuario").equals(pedido.getIdUsuario())) {

                    Date dataNoBanco = participante.getDate("ultimoCheckin");
                    Date dataDoPedido = pedido.getDataCelularAtual();
                    Integer sequenciaAtual = participante.getInteger("sequenciaAtual");

                    if (sequenciaAtual == null) sequenciaAtual = 0;

                    boolean deveAtualizar = false;

                    if (dataNoBanco == null) {
                        deveAtualizar = true;
                    } else {
                        if (isMesmoDia(dataNoBanco, dataDoPedido)) {
                            return new RespostaDeCheckinCompeticao(false, "Você já realizou o check-in hoje!");
                        }

                        if (dataDoPedido.after(dataNoBanco)) {
                            deveAtualizar = true;
                        } else {
                            return new RespostaDeCheckinCompeticao(false, "Data inválida (anterior ao último check-in).");
                        }
                    }

                    if (deveAtualizar) {
                        Bson filtro = Filters.and(
                                Filters.eq("_id", idDaCompeticao),
                                Filters.eq("participantes.idUsuario", pedido.getIdUsuario())
                        );

                        Bson updateOperation = Updates.combine(
                                Updates.set("participantes.$.ultimoCheckin", dataDoPedido),
                                Updates.set("participantes.$.sequenciaAtual", sequenciaAtual + 1)
                        );

                        UpdateResult result = this.colecaoCompeticoes.updateOne(filtro, updateOperation);

                        if (result.wasAcknowledged() && result.getModifiedCount() > 0) {
                            Document competicaoAtualizadaDocument = this.colecaoCompeticoes.find(
                                    Filters.eq("_id", idDaCompeticao)
                            ).first();

                            if (competicaoAtualizadaDocument != null) {
                                List<Document> listaParticipantesMongo = competicaoAtualizadaDocument.getList("participantes", Document.class);
                                List<DocumentoParticipante> listaParticipantes = new ArrayList<>();

                                for (Document participanteDoc : listaParticipantesMongo) {
                                    String idUser = participanteDoc.getString("idUsuario");
                                    String apelido = participanteDoc.getString("apelidoUsuario");
                                    Date ultimo = participanteDoc.getDate("ultimoCheckin");
                                    Integer seq = participanteDoc.getInteger("sequenciaAtual");

                                    // 🔥 BUSCAR FOTO ATUALIZADA também no checkin
                                    Integer idFotoP = 1;
                                    try {
                                        Document usuarioAtual = this.colecaoUsuarios.find(
                                                Filters.eq("_id", new ObjectId(idUser))
                                        ).first();

                                        if (usuarioAtual != null) {
                                            idFotoP = usuarioAtual.getInteger("idFoto", 1);
                                        } else {
                                            idFotoP = participanteDoc.getInteger("idFotoPerfil", 1);
                                        }
                                    } catch (Exception e) {
                                        idFotoP = participanteDoc.getInteger("idFotoPerfil", 1);
                                    }

                                    DocumentoParticipante documentoParticipante = new DocumentoParticipante(idUser, apelido, ultimo, seq, idFotoP);
                                    listaParticipantes.add(documentoParticipante);
                                }

                                Integer idIcone = competicaoAtualizadaDocument.getInteger("idIcone", 1);

                                DocumentoCompeticao competicaoAtualizada = new DocumentoCompeticao(
                                        competicaoAtualizadaDocument.getObjectId("_id").toHexString(),
                                        competicaoAtualizadaDocument.getString("nome"),
                                        competicaoAtualizadaDocument.getString("codigo"),
                                        competicaoAtualizadaDocument.getString("idCriador"),
                                        listaParticipantes,
                                        idIcone
                                );

                                return new RespostaDeCheckinCompeticao(true, "Check-in registrado com sucesso!", competicaoAtualizada);
                            }
                        } else {
                            return new RespostaDeCheckinCompeticao(false, "Falha técnica ao atualizar o banco de dados.");
                        }
                    } else {
                        return new RespostaDeCheckinCompeticao(false, "Check-in já realizado recentemente ou data inválida.");
                    }
                }
            }

            return new RespostaDeCheckinCompeticao(false, "Usuário não encontrado nesta competição.");

        } catch (Exception e) {
            e.printStackTrace();
            return new RespostaDeCheckinCompeticao(false, "Erro interno no servidor: " + e.getMessage());
        }
    }

    public Resposta edicaoCompeticao(PedidoEdicaoCompeticao pedido) {

        try {
            ObjectId idCompeticao = new ObjectId(pedido.getIdCompeticao());

            Bson filtro = Filters.eq("_id", idCompeticao);

            Document competicaoAtual = this.colecaoCompeticoes.find(filtro).first();

            if (competicaoAtual == null) {
                return new Resposta(false, "Competição não encontrada.");
            }

            // Lista que armazena as atualizações que vão ser feitas no banco
            List<Bson> atualizacoes = new ArrayList<>();

            if (pedido.getNovoNome() != null && !pedido.getNovoNome().trim().isEmpty()) {

                String idCriador = competicaoAtual.getString("idCriador");

                // Verifica se o criador já possui outra competição com esse mesmo nome
                long count = this.colecaoCompeticoes.countDocuments(
                        Filters.and(
                                Filters.eq("idCriador", idCriador), // Garante que é do mesmo dono
                                Filters.eq("nome", pedido.getNovoNome()),
                                Filters.ne("_id", idCompeticao) // Exclui a própria competição da busca
                        )
                );

                if (count > 0) {
                    return new Resposta(false, "Você já tem uma competição com este nome.");
                }

                atualizacoes.add(Updates.set("nome", pedido.getNovoNome()));
            }

            if (pedido.getNovoIdIcone() != null) {
                atualizacoes.add(Updates.set("idIcone", pedido.getNovoIdIcone()));
            }

            if (atualizacoes.isEmpty()) {
                return new Resposta(true, "Nada a alterar.");
            }

            this.colecaoCompeticoes.updateOne(filtro, Updates.combine(atualizacoes));

            return new Resposta(true, "Competição atualizada com sucesso");

        } catch (IllegalArgumentException e) {
            return new Resposta(false, "ID da competição inválido.");
        } catch (Exception e) {
            e.printStackTrace();
            return new Resposta(false, "Erro ao atualizar competição: " + e.getMessage());
        }
    }

    public Resposta excluirCompeticao(PedidoExcluirCompeticao pedido) {
        try {
            ObjectId idCompeticao;
            try {
                idCompeticao = new ObjectId(pedido.getIdCompeticao());
            } catch (IllegalArgumentException e) {
                return new Resposta(false, "ID da competição inválido.");
            }

            Bson filtro = Filters.and(
                    Filters.eq("_id", idCompeticao),
                    Filters.eq("idCriador", pedido.getIdUsuario())
            );

            DeleteResult resultado = this.colecaoCompeticoes.deleteOne(filtro);

            if (resultado.getDeletedCount() > 0) {
                return new Resposta(true, "Competição excluída com sucesso.");
            } else {
                return new Resposta(false, "Competição não encontrada ou você não é o dono.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new Resposta(false, "Erro ao excluir competição: " + e.getMessage());
        }
    }

    public Resposta sairDaCompeticao(PedidoSairCompeticao pedido) {
        try {
            ObjectId idCompeticao;
            try {
                idCompeticao = new ObjectId(pedido.getIdCompeticao());
            } catch (IllegalArgumentException e) {
                return new Resposta(false, "ID inválido.");
            }

            Document competicao = this.colecaoCompeticoes.find(Filters.eq("_id", idCompeticao)).first();

            if (competicao == null) {
                return new Resposta(false, "Competição não encontrada.");
            }

            ArrayList<Document> participantes = (ArrayList<Document>) competicao.get("participantes");
            boolean removeu = false;

            // Percorre a lista e remove o participante
            Iterator<Document> iterator = participantes.iterator();
            while (iterator.hasNext()) {
                Document participante = iterator.next();
                if (participante.getString("idUsuario").equals(pedido.getIdUsuario())) {
                    iterator.remove();
                    removeu = true;
                    break;
                }
            }

            if (!removeu) {
                return new Resposta(false, "Você não está nesta competição.");
            }

            // Atualiza o banco com a nova lista
            UpdateResult result = this.colecaoCompeticoes.updateOne(
                    Filters.eq("_id", idCompeticao),
                    Updates.set("participantes", participantes)
            );

            if (result.getModifiedCount() > 0) {
                return new Resposta(true, "Você saiu da competição.");
            } else {
                return new Resposta(false, "Erro ao atualizar dados.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new Resposta(false, "Erro interno: " + e.getMessage());
        }
    }
}