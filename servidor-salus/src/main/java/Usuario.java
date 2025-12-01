import br.com.salus.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public Resposta buscarUsuario (PedidoBuscaUsuario pedido) {
        try {
            ObjectId idParaBuscar = new ObjectId(pedido.getUserId());

            Bson filtro = Filters.eq("_id", idParaBuscar);

            Document documentoUsuario = this.colecaoUsuarios.find(filtro).first();

            if (documentoUsuario != null) {
                DocumentoParticipante documentoParticipante = new DocumentoParticipante(
                        documentoUsuario.getObjectId("_id").toHexString(),
                        documentoUsuario.getString("apelido"),
                        new Date(),
                        1,
                        documentoUsuario.getInteger("idFoto")
                );

                return new RespostaBuscaUsuario(true, "Usuario encontrado", documentoParticipante);
            }

            return new Resposta(false, "Nenhum usuario encontrado");
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

    public Resposta edicaoContaUsuario (PedidoEdicaoConta pedido) {

        try {

            ObjectId idParaBuscar = new ObjectId(pedido.getIdUsuario());

            Bson filtro = Filters.eq("_id", idParaBuscar);

            // lista que armazena as atualizacoes que vao ser feitas no banco pois podem ser mais do que uma
            List<Bson> atualizacoes = new ArrayList<>();

            if (pedido.getNovoApelido() != null) {

                long count = this.colecaoUsuarios.countDocuments(Filters.eq("apelido",pedido.getNovoApelido()));
                if (count > 0 ){
                    return new Resposta(false,"Apelido ja em uso");
                }

                atualizacoes.add(Updates.set("apelido", pedido.getNovoApelido()));
            }

            if (pedido.getNovoEmail() != null) {

                long count = this.colecaoUsuarios.countDocuments(Filters.eq("email", pedido.getNovoEmail()));
                if (count > 0) {
                    return new Resposta(false, "Este novo e-mail já está em uso.");
                }
                atualizacoes.add(Updates.set("email", pedido.getNovoEmail()));
            }

            if (pedido.getNovoIdFotoPerfil() != null) {
                atualizacoes.add(Updates.set("idFoto", pedido.getNovoIdFotoPerfil()));
            }

            if (atualizacoes.isEmpty()) {
                return new Resposta(true, "Nada a alterar.");
            }

            this.colecaoUsuarios.updateOne(filtro, Updates.combine(atualizacoes));

            return new Resposta(true,"Conta atualizada com sucesso");

        }catch (Exception e) {
            e.printStackTrace();
            return new Resposta(false, "Erro ao atualizar conta:" + e.getMessage());
        }
    }

    public Resposta deletarConta (PedidoDeletarConta pedido) {
        try {

            ObjectId idUsuario = new ObjectId(pedido.getIdUsuario());

            this.colecaoUsuarios.deleteOne(Filters.eq(idUsuario));

            return new Resposta(true,"Conta deletada com sucesso");

        }catch (Exception e) {
            e.printStackTrace();
            return new Resposta(false, "Erro ao deletar conta:" + e.getMessage());
        }
    }
}
