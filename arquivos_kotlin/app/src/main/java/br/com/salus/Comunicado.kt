package br.com.salus

import java.io.Serializable

open class Comunicado : Serializable {
    companion object {
        @JvmStatic
        private val serialVersionUID = 1L
    }
}

data class PedidoDeCadastro(
    val nome: String,
    val email: String,
    val senha: String,
    val apelido: String,
    val idFotoPerfil: Int?
) : Comunicado() {
    companion object {
        @JvmStatic
        private val serialVersionUID = 2L
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