package br.com.salus

import android.app.Activity
import android.content.Context
import android.content.Intent

fun mudarTela(context: Context, destination: Class<*>){
    val intent = Intent(context,destination)
    context.startActivity(intent)
}
//fun para mudar de tela passando o iduser
fun mudarTelaFinish(context: Context, destination: Class<*>, userId: String?){
    val USER_ID_KEY = "br.com.salus.USER_ID"

    val intent = Intent(context, destination)
    intent.putExtra(USER_ID_KEY, userId) // Adiciona o userId

    context.startActivity(intent)
    if (context is Activity) {
        context.finish()
    }
}

fun getPlantaDrawableId(context: Context, sequenciaCheckin: Int?, idPlantaFinal: Int?): Int{
    val dias = sequenciaCheckin ?: 0
    val idTipoPlanta = idPlantaFinal ?: 1

    val nomeDoArquivo = when{
        dias <= 2  -> "estagio_1"
        dias <= 5  -> "estagio_2"
        dias <= 10 -> "estagio_3"
        dias <= 15 -> "estagio_4"
        dias <= 20 -> "estagio_5"
        dias < 30  -> "estagio_6"
        else       -> "planta_final_$idTipoPlanta"
    }

    val resourceId = context.resources.getIdentifier(
        nomeDoArquivo,
        "drawable",
        context.packageName
    )

    return if (resourceId != 0) resourceId else R.drawable.estagio_1
}