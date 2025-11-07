package br.com.salus

import android.util.Log
import java.util.Date
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket
import java.io.EOFException
import java.net.SocketException

class ClienteSocket(private val host: String, private val porta: Int) {

    private var socket: Socket? = null
    private var transmissor: ObjectOutputStream? = null
    private var receptor: ObjectInputStream? = null

    fun conectar() {
        try {
            this.socket = Socket(host, porta)

            this.transmissor = ObjectOutputStream(this.socket!!.getOutputStream())
            this.transmissor!!.flush()

            this.receptor = ObjectInputStream(this.socket!!.getInputStream())

            println("Cliente conectado com sucesso a $host:$porta.")

        } catch (e: Exception) {
            System.err.println("Erro ao conectar: ${e.message}")
            throw e
        }
    }

    fun enviar(comunicado: Comunicado) {
        if (this.transmissor == null) throw IllegalStateException("Cliente não conectado.")

        try {
            this.transmissor!!.writeObject(comunicado)
            this.transmissor!!.flush()
            println("Enviado: $comunicado")
        } catch (e: Exception) {
            System.err.println("Erro ao enviar objeto: ${e.message}")
            throw e
        }
    }

    fun receber(): Comunicado {
        if (this.receptor == null) throw IllegalStateException("Cliente não conectado.")

        try {
            val resposta = this.receptor!!.readObject() as Comunicado
            println("Recebido: $resposta")
            return resposta
        } catch (e: EOFException) {

            throw SocketException("Conexão fechada pelo servidor.")
        } catch (e: Exception) {
            System.err.println("Erro ao receber objeto: ${e.message}")
            throw e
        }
    }

    fun desconectar() {
        try {
            enviar(PedidoParaSair())
        } catch (e: Exception) {

        }

        try {
            receptor?.close()
            transmissor?.close()
            socket?.close()
            println("Cliente desconectado.")
        } catch (e: Exception) {
            System.err.println("Erro ao fechar conexão: ${e.message}")
        }
    }
}

fun searchForEmail(email: String) {
    val HOST = "localhost"
    val PORTA = 3000
    val cliente = ClienteSocket(HOST, PORTA)

    try {
        cliente.conectar()
        val pedido = PedidoBuscaEmail(email)

        cliente.enviar(pedido)
        val resposta = cliente.receber()

        if (resposta is Resposta) {
            if (!resposta.sucesso) {
                Log.d("teste Server", resposta.mensagem)
            } else {
                Log.d("teste Server", resposta.mensagem)
            }
        }
    } catch (e: Exception) {
        Log.e("NetworkTest", "Ocorreu um erro geral:", e)

    } finally {
        Log.d("NetworkTest", "Bloco finally executado, desconectando.")
        cliente.desconectar()
    }
}

fun userSignUp(
    nome: String,
    email: String,
    senha: String,
    apelido: String,
    idFotoPerfil: Int
) {
    val HOST = "localhost"
    val PORTA = 3000
    val cliente = ClienteSocket(HOST, PORTA)

    try {
        cliente.conectar()
        val pedido = PedidoDeCadastro(
            nome = nome,
            email = email,
            senha = senha,
            apelido = apelido,
            idFotoPerfil = idFotoPerfil,
            dataHoraCriacao = Date()
        )

        cliente.enviar(pedido)
        val resposta = cliente.receber()

        if (resposta is Resposta) {
            if (resposta.sucesso) {
                Log.d("teste Server", "Usuario cadastrado com sucesso")
            } else {
                Log.d("teste Server", "Falha ao cadastrar um novo usuario: " + resposta.mensagem)
            }
        } else {
            println("Resposta inesperada recebida: $resposta")
        }

    } catch (e: Exception) {
        Log.e("NetworkTest", "Ocorreu um erro geral:", e)

    } finally {
        Log.d("NetworkTest", "Bloco finally executado, desconectando.")
        cliente.desconectar()
    }
}

fun userSignIn(email: String, senha: String) {
    val HOST = "localhost"
    val PORTA = 3000
    val cliente = ClienteSocket(HOST, PORTA)

    try {
        cliente.conectar()
        val pedido = PedidoDeLogin(email, senha)

        cliente.enviar(pedido)
        val resposta = cliente.receber()

        if (resposta is Resposta) {
            if (resposta.sucesso) {
                Log.d("teste Server", "Usuario logado com sucesso")
            } else {
                Log.d("teste Server", "Falha ao logar: " + resposta.mensagem)
            }
        } else {
            println("Resposta inesperada recebida: $resposta")
        }
    } catch (e: Exception) {
        Log.e("NetworkTest", "Ocorreu um erro geral:", e)

    } finally {
        Log.d("NetworkTest", "Bloco finally executado, desconectando.")
        cliente.desconectar()
    }
}

fun newHabit(nome: String, userId: String) {
    val HOST = "localhost"
    val PORTA = 3000
    val cliente = ClienteSocket(HOST, PORTA)

    try {
        cliente.conectar()
        val pedido = PedidoDeNovoHabito(
            nome = nome,
            sequenciaCheckin = 0,
            ultimoCheckin = null,
            userId = userId
        )

        cliente.enviar(pedido)
        val resposta = cliente.receber()

        if (resposta is Resposta) {
            if (resposta.sucesso) {
                Log.d("teste Server", "Habito criado com sucesso")
            } else {
                Log.d("teste Server", "Falha ao criar um novo habito: " + resposta.mensagem)
            }
        } else {
            println("Resposta inesperada recebida: $resposta")
        }

    } catch (e: Exception) {
        Log.e("NetworkTest", "Ocorreu um erro geral:", e)

    } finally {
        Log.d("NetworkTest", "Bloco finally executado, desconectando.")
        cliente.desconectar()
    }
}