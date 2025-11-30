package br.com.salus

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.SocketException
import java.util.Date

object NetworkManager {

    private const val HOST = "10.0.2.2"
    private const val PORTA = 3000

    private var cliente: ClienteSocket? = null
    private const val TAG = "NetworkManager"

    fun isConectado(): Boolean {
        return cliente?.isConectado() ?: false
    }

    private suspend fun conectar() {
        withContext(Dispatchers.IO) {
            if (!isConectado()){
                try {
                    cliente = ClienteSocket(HOST, PORTA)
                    cliente?.conectar()
                    Log.d(TAG, "Conectado ao servidor")
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao conectar ao servidor", e)
                    cliente = null
                    throw e
                }
            }
        }
    }

    suspend fun desconectar() {
        withContext(Dispatchers.IO) {
            try {
                cliente?.desconectar()
                Log.d(TAG, "Desconectado do servidor")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao desconectar do servidor", e)
            } finally {
                cliente = null
            }
        }
    }

    private suspend fun <T : Comunicado> enviarRequisicao(pedido: T): Comunicado {
        return withContext(Dispatchers.IO) {
            if (!isConectado()) {
                Log.e(TAG, "Requisição falhou: Não conectado.")
                return@withContext Resposta(false, "Sem conexão com o servidor.")
            }

            try {
                Log.d(TAG, "Enviando pedido: ${pedido::class.simpleName}")
                cliente!!.enviar(pedido)
                Log.d(TAG, "Aguardando resposta...")
                val resposta = cliente!!.receber()
                Log.d(TAG, "Resposta recebida: ${resposta::class.simpleName}")
                return@withContext resposta

            } catch (e: SocketException) {
                Log.e(TAG, "ERRO (SocketException): Conexão perdida: ${e.message}", e)
                desconectar()
                return@withContext Resposta(false, "Conexão com o servidor foi perdida: ${e.message}")

            } catch (e: Exception) {
                val msgErro = e.message ?: "Exceção desconhecida"
                Log.e(TAG, "ERRO (Exception) na requisição: $msgErro", e)
                e.printStackTrace()
                return@withContext Resposta(false, "Erro de rede: $msgErro")
            }
        }
    }

    suspend fun userSignIn(
        email: String,
        senha: String
    ): RespostaDeLogin {
        return try {
            conectar()
            if (!isConectado()) {
                Log.e(TAG, "userSignIn: Não foi possível conectar")
                return RespostaDeLogin(false, "Não foi possível conectar ao servidor.", null)
            }

            Log.d(TAG, "userSignIn: Enviando pedido de login para: $email")
            val pedido = PedidoDeLogin(email, senha)
            val resposta = enviarRequisicao(pedido)

            Log.d(TAG, "userSignIn: Tipo da resposta recebida: ${resposta::class.simpleName}")

            val respostaLogin: RespostaDeLogin = when (resposta) {
                is RespostaDeLogin -> {
                    Log.d(TAG, "userSignIn: RespostaDeLogin - sucesso: ${resposta.sucesso}, userId: ${resposta.userId}")
                    resposta
                }
                is Resposta -> {
                    Log.w(TAG, "userSignIn: Resposta genérica - sucesso: ${resposta.sucesso}, msg: ${resposta.mensagem}")
                    RespostaDeLogin(resposta.sucesso, resposta.mensagem, null)
                }
                else -> {
                    Log.e(TAG, "userSignIn: Resposta inesperada: ${resposta::class.simpleName}")
                    RespostaDeLogin(false, "Resposta inesperada do servidor.", null)
                }
            }

            if (!respostaLogin.sucesso) {
                Log.w(TAG, "userSignIn: Login falhou, desconectando")
                desconectar()
            } else {
                Log.d(TAG, "userSignIn: Login bem-sucedido!")
            }

            respostaLogin

        } catch (e: Exception) {
            Log.e(TAG, "userSignIn: Exceção: ${e.message}", e)
            e.printStackTrace()
            desconectar()
            RespostaDeLogin(false, "Erro ao tentar fazer login: ${e.message}", null)
        }
    }

    suspend fun deletarConta(userId: String): Resposta {
        return try {
            Log.d(TAG, "deletarConta: Iniciando pedido de exclusão para o ID: $userId")

            val pedido = PedidoDeletarConta(userId)

            val resposta = enviarRequisicao(pedido)

            val respostaFinal = resposta as? Resposta
                ?: Resposta(false, "Resposta inesperada do servidor.")


            if (respostaFinal.sucesso) {
                Log.d(TAG, "deletarConta: Conta excluída com sucesso. Fechando conexão.")
                desconectar()
            }

            respostaFinal
        } catch (e: Exception) {
            Log.e(TAG, "deletarConta: Erro crítico", e)
            Resposta(false, "Erro ao deletar conta: ${e.message}")
        }
    }

    suspend fun editarConta(
        userId: String,
        novoApelido: String? = null,
        novoEmail: String? = null,
        novoIdFotoPerfil: Int? = null
    ): Resposta {
        return try {
            Log.d(TAG, "editarConta: Iniciando edição para ID: $userId")

            val pedido = PedidoEdicaoConta(
                idUsuario = userId,
                novoApelido = novoApelido,
                novoEmail = novoEmail,
                novoIdFotoPerfil = novoIdFotoPerfil
            )

            val resposta = enviarRequisicao(pedido)

            resposta as? Resposta ?: Resposta(false, "Resposta inesperada do servidor.")

        } catch (e: Exception) {
            Log.e(TAG, "editarConta: Erro", e)
            Resposta(false, "Erro ao editar conta: ${e.message}")
        }
    }

    suspend fun searchForEmail(email: String): Resposta {
        return try {
            val pedido = PedidoBuscaEmail(email)
            val resposta = enviarRequisicao(pedido)
            resposta as? Resposta ?: Resposta(false, "Resposta inesperada.")
        } catch (e: Exception) {
            Log.e(TAG, "searchForEmail: Erro", e)
            Resposta(false, "Erro ao buscar email: ${e.message}")
        }
    }

    suspend fun userSignUp(
        nome: String,
        email: String,
        senha: String,
        apelido: String,
        idFotoPerfil: Int
    ): RespostaPedidoDeCadastro {
        return try {
            conectar()
            if (!isConectado()){
                return RespostaPedidoDeCadastro(false, "Não foi possível conectar ao servidor.")
            }

            val pedido = PedidoDeCadastro(
                nome = nome,
                email = email,
                senha = senha,
                apelido = apelido,
                idFotoPerfil = idFotoPerfil,
                dataHoraCriacao = Date()
            )

            val resposta = enviarRequisicao(pedido) as? RespostaPedidoDeCadastro
                ?: RespostaPedidoDeCadastro (false, "Resposta inesperada.")

            if (!resposta.sucesso) {
                Log.w(TAG, "userSignUp: Cadastro falhou, desconectando...")
                desconectar()
            } else {
                Log.d(TAG, "userSignUp: Cadastro realizado! Mantendo conexão ativa.")
            }

            resposta
        } catch (e: Exception) {
            Log.e(TAG, "userSignUp: Erro", e)
            desconectar()
            RespostaPedidoDeCadastro (false, "Erro ao cadastrar: ${e.message}")
        }
    }

    suspend fun newHabit(
        nome: String,
        userId: String,
        idFotoPlanta: Int
    ): Resposta {
        return try {
            val pedido = PedidoDeNovoHabito(
                nome = nome,
                sequenciaCheckin = 0,
                ultimoCheckin = null,
                userId = userId,
                idFotoPlanta = idFotoPlanta
            )

            enviarRequisicao(pedido) as? Resposta
                ?: Resposta(false, "Resposta inesperada.")
        } catch (e: Exception) {
            Log.e(TAG, "newHabit: Erro", e)
            Resposta(false, "Erro ao criar hábito: ${e.message}")
        }
    }

    suspend fun getHabitos(userId: String): RespostaListaHabitos {
        return try {
            val pedido = PedidoListaHabitos(userId)
            val resposta = enviarRequisicao(pedido)

            when (resposta) {
                is RespostaListaHabitos -> resposta
                is Resposta -> RespostaListaHabitos(false, resposta.mensagem, null)
                else -> RespostaListaHabitos(false, "Resposta inesperada.", null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "getHabitos: Erro", e)
            RespostaListaHabitos(false, "Erro ao buscar hábitos: ${e.message}", null)
        }
    }

    suspend fun editarHabito(
        habitId: String,
        novoNome: String? = null,
        novaFotoPlanta: Int? = null
    ): Resposta {
        return try {
            Log.d(TAG, "editarHabito: Iniciando edição para Hábito ID: $habitId")

            if (novoNome == null && novaFotoPlanta == null) {
                return Resposta(false, "Nenhum dado para alterar foi fornecido.")
            }

            val pedido = PedidoEdicaoHabito(
                idHabito = habitId,
                novoNomeHabito = novoNome,
                novoIdFotoPlanta = novaFotoPlanta
            )

            val resposta = enviarRequisicao(pedido)

            resposta as? Resposta ?: Resposta(false, "Resposta inesperada do servidor.")

        } catch (e: Exception) {
            Log.e(TAG, "editarHabito: Erro", e)
            Resposta(false, "Erro ao editar hábito: ${e.message}")
        }
    }

    suspend fun excluirHabito(habitId: String, userId: String): Resposta {
        return try {
            val pedido = PedidoExcluirHabito(habitId, userId)
            val resposta = enviarRequisicao(pedido)

            resposta as? Resposta ?: Resposta(false, "Resposta inesperada.")
        } catch (e: Exception) {
            Log.e(TAG, "excluirHabito: Erro", e)
            Resposta(false, "Erro ao excluir hábito: ${e.message}")
        }
    }

    suspend fun realizarCheckin(habitoId: String): RespostaDeCheckin {
        return try {
            val pedido = PedidoDeCheckin(habitoId)
            val resposta = enviarRequisicao(pedido)

            when (resposta) {
                is RespostaDeCheckin -> resposta
                is Resposta -> RespostaDeCheckin(false, resposta.mensagem, null)
                else -> RespostaDeCheckin(false, "Resposta inesperada.", null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "realizarCheckin: Erro", e)
            RespostaDeCheckin(false, "Erro ao fazer check-in: ${e.message}", null)
        }
    }

    suspend fun criarCompeticao(nome: String, idCriador: String, idIcone: Int): RespostaDeNovaCompeticao {
        return try {
            Log.d(TAG, "criarCompeticao: nome=$nome, criador=$idCriador, icone=$idIcone")
            val pedido = PedidoDeNovaCompeticao(nome, idCriador, idIcone)
            val resposta = enviarRequisicao(pedido)

            when (resposta) {
                is RespostaDeNovaCompeticao -> {
                    Log.d(TAG, "Competição criada! Código: ${resposta.codigo}")
                    resposta
                }
                is Resposta -> {
                    Log.w(TAG, "Resposta genérica: ${resposta.mensagem}")
                    RespostaDeNovaCompeticao(false, resposta.mensagem, null, null)
                }
                else -> {
                    Log.e(TAG, "Resposta inesperada")
                    RespostaDeNovaCompeticao(false, "Resposta inesperada.", null, null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "criarCompeticao: Erro", e)
            RespostaDeNovaCompeticao(false, "Erro ao criar competição: ${e.message}", null, null)
        }
    }

    suspend fun entrarNaCompeticao(codigo: String, idUsuario: String): RespostaEntrarCompeticao {
        return try {
            Log.d(TAG, "entrarNaCompeticao: codigo=$codigo, usuario=$idUsuario")
            val pedido = PedidoEntrarCompeticao(codigo, idUsuario)
            val resposta = enviarRequisicao(pedido)

            when (resposta) {
                is RespostaEntrarCompeticao -> {
                    Log.d(TAG, "Resposta recebida: ${resposta.mensagem}")
                    resposta
                }
                is Resposta -> {
                    Log.w(TAG, "Resposta genérica: ${resposta.mensagem}")
                    RespostaEntrarCompeticao(false, resposta.mensagem, null, null)
                }
                else -> {
                    Log.e(TAG, "Resposta inesperada")
                    RespostaEntrarCompeticao(false, "Resposta inesperada.", null, null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "entrarNaCompeticao: Erro", e)
            RespostaEntrarCompeticao(false, "Erro ao entrar na competição: ${e.message}", null, null)
        }
    }

    suspend fun buscarCompeticoes(userId: String): RespostaBuscaCompeticao {
        return try {
            val pedido = PedidoBuscaCompeticao(userId)
            val resposta = enviarRequisicao(pedido)

            when (resposta) {
                is RespostaBuscaCompeticao -> resposta
                is Resposta -> RespostaBuscaCompeticao(false, resposta.mensagem, null)
                else -> RespostaBuscaCompeticao(false, "Resposta inesperada.", null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "buscarCompeticoes: Erro", e)
            RespostaBuscaCompeticao(false, "Erro ao buscar competições: ${e.message}", null)
        }
    }

    suspend fun realizarCheckinCompeticao(idCompeticao: String, idUsuario: String): RespostaDeCheckinCompeticao {
        return try {
            // Usamos Date() para pegar a data atual do celular
            val pedido = PedidoDeCheckinCompeticao(idCompeticao, idUsuario, Date())
            val resposta = enviarRequisicao(pedido)

            when (resposta) {
                is RespostaDeCheckinCompeticao -> resposta
                is Resposta -> RespostaDeCheckinCompeticao(false, resposta.mensagem, null)
                else -> RespostaDeCheckinCompeticao(false, "Resposta inesperada.", null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "realizarCheckinCompeticao: Erro", e)
            RespostaDeCheckinCompeticao(false, "Erro ao fazer check-in na competição: ${e.message}", null)
        }
    }
}