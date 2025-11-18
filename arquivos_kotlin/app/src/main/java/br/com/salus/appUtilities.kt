package br.com.salus

import android.app.Activity
import android.content.Context
import android.content.Intent

fun mudarTela(context: Context, destination: Class<*>){
    val intent = Intent(context,destination)
    context.startActivity(intent)
}

fun mudarTelaFinish(context: Context, destination: Class<*>){
    val intent = Intent(context, destination)
    context.startActivity(intent)
    if (context is Activity) {
        context.finish()
    }
}