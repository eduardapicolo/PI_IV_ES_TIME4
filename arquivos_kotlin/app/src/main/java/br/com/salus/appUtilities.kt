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

fun mudarTelaFinish(context: Context, destination: Class<*>){
    val intent = Intent(context, destination)
    context.startActivity(intent)
    if (context is Activity) {
        context.finish()
    }
}