package br.botaovermelho

import android.Manifest
import android.content.Context

object Permissions {

    fun needed(context: Context): List<String> {
        val list = mutableListOf(Manifest.permission.SEND_SMS, Manifest.permission.CALL_PHONE)
        if (Config(context).sendLocation) {
            list += Manifest.permission.ACCESS_FINE_LOCATION
            list += Manifest.permission.ACCESS_COARSE_LOCATION
        }
        return list
    }

    fun missing(context: Context): List<String> {
        val miss = needed(context).filter { !Emergency.has(context, it) }
        // Para a localização basta uma das duas (aproximada ou precisa).
        return if (Locator.hasPermission(context)) {
            miss.filter {
                it != Manifest.permission.ACCESS_FINE_LOCATION &&
                    it != Manifest.permission.ACCESS_COARSE_LOCATION
            }
        } else miss
    }

    fun describe(perms: List<String>): String = perms.map {
        when (it) {
            Manifest.permission.SEND_SMS -> "SMS"
            Manifest.permission.CALL_PHONE -> "ligação"
            else -> "localização"
        }
    }.distinct().joinToString(", ")
}
