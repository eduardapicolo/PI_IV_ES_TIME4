package br.com.salus

import java.io.Serializable
import java.util.Date

open class Comunicado : Serializable {
    companion object {
        @JvmStatic
        private val serialVersionUID = 1L
    }
}

data class PedidoBuscaEmail(
    val email: String
) : Comunicado() {
    companion object {
        @JvmStatic
        private val serialVersionUID = 5L
    }
}


data class PedidoDeCadastro(
    val nome: String,
    val email: String,
    val senha: String,
    val apelido: String,
    val idFotoPerfil: Int?,
    val dataHoraCriacao: Date
) : Comunicado() {
    companion object {
        @JvmStatic
        private val serialVersionUID = 2L
    }
}

data class PedidoDeLogin(
    val email: String,
    val senha: String
) : Comunicado() {
    companion object {
        @JvmStatic
        private val serialVersionUID = 6L
    }
}

data class PedidoDeNovoHabito(
    val nome: String,
    val sequenciaCheckin: Int?,
    val ultimoCheckin: Date?,
    val userId: String
) : Comunicado() {
    companion object {
        @JvmStatic
        private val serialVersionUID = 7L
    }
}

data class Resposta(
    val sucesso: Boolean,
    val mensagem: String
) : Comunicado() {
    companion object {
        @JvmStatic
        private val serialVersionUID = 3L
    }
}

class PedidoParaSair : Comunicado() {
    companion object {
        @JvmStatic
        private val serialVersionUID = 4L // Número diferente para a classe filha
    }
}