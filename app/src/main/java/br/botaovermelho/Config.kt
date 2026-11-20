package br.botaovermelho

import android.content.Context

/** O que foi configurado uma vez (por quem instala o app), guardado no aparelho. */
class Config(context: Context) {
    private val prefs = context.getSharedPreferences("config", Context.MODE_PRIVATE)

    /** Quem recebe a ligação e a mensagem, ex.: "Rafael". */
    var contactName: String
        get() = prefs.getString("contactName", "")!!
        set(v) = prefs.edit().putString("contactName", v.trim()).apply()

    /** Número como foi digitado; use [number] para ligar. */
    var rawNumber: String
        get() = prefs.getString("number", "")!!
        set(v) = prefs.edit().putString("number", v.trim()).apply()

    /** Como ela aparece na mensagem, ex.: "Mãe" ou "Dona Maria". */
    var senderName: String
        get() = prefs.getString("senderName", "")!!
        set(v) = prefs.edit().putString("senderName", v.trim()).apply()

    var sendLocation: Boolean
        get() = prefs.getBoolean("sendLocation", true)
        set(v) = prefs.edit().putBoolean("sendLocation", v).apply()

    /** Hora (System.currentTimeMillis) do último pedido de ajuda, 0 se nunca. */
    var lastTrigger: Long
        get() = prefs.getLong("lastTrigger", 0L)
        set(v) = prefs.edit().putLong("lastTrigger", v).apply()

    val number: String get() = normalize(rawNumber)

    val isConfigured: Boolean get() = number.count { it.isDigit() } >= 8

    /** Nome para mostrar nos textos: "Rafael", ou "o contato" se não houver. */
    val contactLabel: String get() = contactName.ifBlank { "o contato" }

    companion object {
        /**
         * Deixa só dígitos (e um "+" no começo). Número brasileiro com DDD
         * (10 ou 11 dígitos) vira +55..., que funciona para ligar e mandar SMS
         * de qualquer DDD e de qualquer operadora.
         */
        fun normalize(raw: String): String {
            val plus = raw.trim().startsWith("+")
            val digits = raw.filter { it.isDigit() }
            // "047 99999-8888": o 0 do DDD sem código de operadora
            val semZero = if (digits.startsWith("0")) digits.drop(1) else digits
            return when {
                plus -> "+$digits"
                semZero.length == 10 || semZero.length == 11 -> "+55$semZero"
                else -> digits
            }
        }
    }
}
