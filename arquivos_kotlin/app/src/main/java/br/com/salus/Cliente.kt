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
    private val TAG = "ClienteSocket"

    fun isConectado(): Boolean {
        return socket != null && socket!!.isConnected && !socket!!.isClosed
    }

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
