package br.com.salus

import java.io.Serializable
import java.util.Date

open class Comunicado : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}

class PedidoBuscaEmail(val email: String) : Comunicado() {
    companion object {
        private const val serialVersionUID = 5L
    }
}

class PedidoDeCadastro(
    val nome: String,
    val email: String,
    val senha: String,
    val apelido: String,
    val idFotoPerfil: Int?,
    val dataHoraCriacao: Date
) : Comunicado() {
    companion object {
        private const val serialVersionUID = 2L
    }
}

class PedidoDeLogin(
    val email: String,
    val senha: String
) : Comunicado() {
    companion object {
        private const val serialVersionUID = 6L
    }
}

class PedidoDeletarConta(
    val idUsuario: String
) : Comunicado() {
    companion object {
        private const val serialVersionUID = 106L
    }
}

class PedidoEdicaoConta(
    val idUsuario: String,
    val novoApelido: String?,     // Pode ser nulo
    val novoEmail: String?,       // Pode ser nulo
    val novoIdFotoPerfil: Int?    // Pode ser nulo
) : Comunicado() {
    companion object {
        private const val serialVersionUID = 105L
    }
}


class PedidoDeNovoHabito(
    val nome: String,
    val sequenciaCheckin: Int?,
    val ultimoCheckin: Date?,
    val userId: String,
    val idFotoPlanta: Int?
) : Comunicado() {
    companion object {
        private const val serialVersionUID = 7L
    }
}

class PedidoListaHabitos(val userId: String) : Comunicado() {
    companion object {
        private const val serialVersionUID = 8L
    }
}

class PedidoEdicaoHabito(
    val idHabito: String,
    val novoNomeHabito: String?,
    val novoIdFotoPlanta: Int?
) : Comunicado() {
    companion object {
        private const val serialVersionUID = 110L
    }
}

class PedidoDeCheckin(val idHabito: String) : Comunicado() {
    companion object {
        private const val serialVersionUID = 11L
    }
}

class PedidoDeCheckinCompeticao(val idCompeticao: String,val idUsuario : String, val dataCelularAtual : Date) : Comunicado() {
    companion object {
        private const val serialVersionUID = 76L
    }
}

class PedidoDeNovaCompeticao(
    val nome: String,
    val idCriador: String,
    val idIcone: Int
) : Comunicado() {
    companion object {
        private const val serialVersionUID = 14L
    }
}

class PedidoEntrarCompeticao(
    val codigo: String,
    val idUsuario: String
) : Comunicado() {
    companion object {
        private const val serialVersionUID = 16L
    }
}

class PedidoBuscaCompeticao(
    val idUsuario: String
) : Comunicado() {
    companion object {
        private const val serialVersionUID = 18L
    }
}

class PedidoExcluirHabito(
    val idHabito: String,
    val idUsuario: String
) : Comunicado() {
    companion object {
        private const val serialVersionUID = 22L
    }
}

class PedidoParaSair : Comunicado() {
    companion object {
        private const val serialVersionUID = 4L
    }
}

open class Resposta : Comunicado {
    val sucesso: Boolean
    val mensagem: String

    constructor(sucesso: Boolean, mensagem: String) : super() {
        this.sucesso = sucesso
        this.mensagem = mensagem
    }

    companion object {
        private const val serialVersionUID = 3L
    }

    override fun toString(): String {
        return "sucesso: $sucesso mensagem: $mensagem"
    }
}

class RespostaPedidoDeCadastro : Resposta {
    val userId: String?

    constructor(sucesso: Boolean, msg: String, userId: String?): super(sucesso, msg) {
        this.userId = userId
    }

    constructor(sucesso: Boolean, msg: String): super(sucesso, msg) {
        this.userId = null
    }

    companion object {
        private const val serialVersionUID = 101L
    }
}

class RespostaDeLogin : Resposta {
    val userId: String?

    constructor(sucesso: Boolean, msg: String, userId: String?): super(sucesso, msg) {
        this.userId = userId
    }

    constructor(sucesso: Boolean, msg: String): super(sucesso, msg) {
        this.userId = null
    }

    companion object {
        private const val serialVersionUID = 13L
    }
}

class RespostaListaHabitos : Resposta {
    val habitos: List<DocumentoHabito>?

    constructor(sucesso: Boolean, mensagem: String, habitos: List<DocumentoHabito>?): super(sucesso, mensagem) {
        this.habitos = habitos
    }

    constructor(sucesso: Boolean, mensagem: String): super(sucesso, mensagem) {
        this.habitos = null
    }

    companion object {
        private const val serialVersionUID = 9L
    }
}

class RespostaDeCheckin : Resposta {
    val habitoAtualizado: DocumentoHabito?

    constructor(sucesso: Boolean, msg: String, habitoAtualizado: DocumentoHabito?) : super(sucesso, msg) {
        this.habitoAtualizado = habitoAtualizado
    }

    constructor(sucesso: Boolean, msg: String) : super(sucesso, msg) {
        this.habitoAtualizado = null
    }

    companion object {
        private const val serialVersionUID = 12L
    }
}

class RespostaDeCheckinCompeticao : Resposta {
    val competicaoAtualizada: DocumentoCompeticao?

    constructor(sucesso: Boolean, msg: String, competicaoAtualizada: DocumentoCompeticao?) : super(sucesso, msg) {
        this.competicaoAtualizada = competicaoAtualizada
    }

    constructor(sucesso: Boolean, msg: String) : super(sucesso, msg) {
        this.competicaoAtualizada = null
    }

    companion object {
        private const val serialVersionUID = 77L
    }
}

class RespostaDeNovaCompeticao : Resposta {
    val idCompeticao: String?
    val codigo: String?

    constructor(sucesso: Boolean, mensagem: String, idCompeticao: String?, codigo: String?) : super(sucesso, mensagem) {
        this.idCompeticao = idCompeticao
        this.codigo = codigo
    }

    constructor(sucesso: Boolean, mensagem: String) : super(sucesso, mensagem) {
        this.idCompeticao = null
        this.codigo = null
    }

    companion object {
        private const val serialVersionUID = 15L
    }
}

class RespostaEntrarCompeticao : Resposta {
    val idCompeticao: String?
    val nomeCompeticao: String?

    constructor(sucesso: Boolean, mensagem: String, idCompeticao: String?, nomeCompeticao: String?) : super(sucesso, mensagem) {
        this.idCompeticao = idCompeticao
        this.nomeCompeticao = nomeCompeticao
    }

    constructor(sucesso: Boolean, mensagem: String) : super(sucesso, mensagem) {
        this.idCompeticao = null
        this.nomeCompeticao = null
    }

    companion object {
        private const val serialVersionUID = 17L
    }
}

class RespostaBuscaCompeticao : Resposta {
    val competicoes: List<DocumentoCompeticao>?

    constructor(sucesso: Boolean, mensagem: String, competicoes: List<DocumentoCompeticao>?) : super(sucesso, mensagem) {
        this.competicoes = competicoes
    }

    constructor(sucesso: Boolean, mensagem: String) : super(sucesso, mensagem) {
        this.competicoes = null
    }

    companion object {
        private const val serialVersionUID = 19L
    }
}

class DocumentoHabito(
    val id: String,
    val nome: String,
    val sequenciaCheckin: Int?,
    val ultimoCheckin: Date?,
    val idFotoPlanta: Int?
) : Serializable {
    companion object {
        private const val serialVersionUID = 10L
    }
}

class DocumentoParticipante(
    val idUsuario: String,
    val apelidoUsuario: String,
    val ultimoCheckin: Date?,
    val sequencia: Int?,
    val idFotoPerfil: Int?
) : Serializable {
    companion object {
        private const val serialVersionUID = 20L
    }
}

class DocumentoCompeticao(
    val id: String,
    val nome: String,
    val codigo: String,
    val idCriador: String,
    val participantes: List<DocumentoParticipante>,
    val idIcone: Int
) : Serializable {
    companion object {
        private const val serialVersionUID = 21L
    }
}

class PedidoEdicaoCompeticao(
    val idCompeticao: String,
    val novoNome: String?,
    val novoIdIcone: Int?
) : Comunicado() {
    companion object {
        private const val serialVersionUID = 120L
    }
}