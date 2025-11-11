package br.com.salus

import java.io.Serializable
import java.util.Date

// Classe base - deve corresponder exatamente ao Java
open class Comunicado : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}

// ============================================
// PEDIDOS (Request classes)
// ============================================

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

class PedidoDeNovoHabito(
    val nome: String,
    val sequenciaCheckin: Int?,
    val ultimoCheckin: Date?,
    val userId: String
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

class PedidoDeCheckin(val idHabito: String) : Comunicado() {
    companion object {
        private const val serialVersionUID = 11L
    }
}

class PedidoParaSair : Comunicado() {
    companion object {
        private const val serialVersionUID = 4L
    }
}

// ============================================
// RESPOSTAS (Response classes)
// IMPORTANTE: Devem corresponder EXATAMENTE às classes Java
// ============================================

// Resposta base - exatamente como no Java
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

// RespostaDeLogin - exatamente como no Java
class RespostaDeLogin : Resposta {
    val userId: String?

    // Construtor com userId
    constructor(sucesso: Boolean, msg: String, userId: String?) : super(sucesso, msg) {
        this.userId = userId
    }

    // Construtor sem userId
    constructor(sucesso: Boolean, msg: String) : super(sucesso, msg) {
        this.userId = null
    }

    companion object {
        private const val serialVersionUID = 13L
    }
}

// RespostaListaHabitos - exatamente como no Java
class RespostaListaHabitos : Resposta {
    val habitos: List<DocumentoHabito>?

    constructor(sucesso: Boolean, mensagem: String, habitos: List<DocumentoHabito>?) : super(sucesso, mensagem) {
        this.habitos = habitos
    }

    constructor(sucesso: Boolean, mensagem: String) : super(sucesso, mensagem) {
        this.habitos = null
    }

    companion object {
        private const val serialVersionUID = 9L
    }
}

// RespostaDeCheckin - exatamente como no Java
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

// ============================================
// DTOs (Data Transfer Objects)
// ============================================

class DocumentoHabito(
    val id: String,
    val nome: String,
    val sequenciaCheckin: Int?,
    val ultimoCheckin: Date?
) : Serializable {
    companion object {
        private const val serialVersionUID = 10L
    }
}