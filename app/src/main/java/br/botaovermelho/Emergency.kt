package br.botaovermelho

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.telephony.SmsManager
import android.text.format.DateFormat
import android.widget.Toast
import java.util.Date
import java.util.Locale

/** O que acontece quando ela aperta o botão. */
object Emergency {

    private var lastPress = 0L

    /**
     * Manda a mensagem e liga, nessa ordem e sem esperar: o SMS sai em
     * segundo plano enquanto a ligação começa. Se a localização guardada
     * no celular for velha ou imprecisa, um serviço busca a atual e manda
     * uma segunda mensagem com ela.
     */
    fun trigger(activity: Activity) {
        val cfg = Config(activity)
        if (!cfg.isConfigured) {
            Toast.makeText(activity, "Falta configurar o número.", Toast.LENGTH_LONG).show()
            activity.startActivity(Intent(activity, SettingsActivity::class.java))
            return
        }
        // Dois toques seguidos contam como um só.
        val now = SystemClock.elapsedRealtime()
        if (now - lastPress < 10_000) return
        lastPress = now
        cfg.lastTrigger = System.currentTimeMillis()

        // 1. Mensagem
        val loc = if (cfg.sendLocation) Locator.lastKnown(activity) else null
        val smsOk = sendSms(activity, cfg.number, alertText(activity, cfg, loc))

        // 2. Localização atualizada (precisa começar antes da ligação, com o app na frente)
        if (cfg.sendLocation && Locator.hasPermission(activity) && !Locator.isGood(loc)) {
            LocationService.start(activity)
        }

        // 3. Ligação
        call(activity, cfg.number)

        if (!smsOk) {
            Toast.makeText(activity, "Não consegui mandar a mensagem. Ligando…", Toast.LENGTH_LONG).show()
        }
    }

    fun call(activity: Activity, number: String) {
        val uri = Uri.fromParts("tel", number, null)
        val intent = if (has(activity, Manifest.permission.CALL_PHONE)) {
            Intent(Intent.ACTION_CALL, uri)
        } else {
            // Sem permissão o Android só deixa abrir o discador com o número já digitado.
            Intent(Intent.ACTION_DIAL, uri)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            activity.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(activity, "Não consegui ligar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /** Devolve false se não deu para entregar a mensagem ao sistema. */
    fun sendSms(context: Context, number: String, text: String): Boolean {
        if (!has(context, Manifest.permission.SEND_SMS)) return false
        return try {
            val sms = if (Build.VERSION.SDK_INT >= 31) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION") SmsManager.getDefault()
            }
            val parts = sms.divideMessage(text)
            sms.sendMultipartTextMessage(number, null, parts, null, null)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun alertText(context: Context, cfg: Config, loc: Location?): String {
        val who = cfg.senderName
        val sb = StringBuilder("EMERGÊNCIA! ")
        sb.append(
            if (who.isBlank()) "O botão vermelho foi apertado e o celular está ligando para você."
            else "$who apertou o botão vermelho e está ligando para você."
        )
        if (loc != null) {
            sb.append("\nOnde está: ").append(describe(context, loc))
        } else if (cfg.sendLocation) {
            sb.append("\nBuscando a localização…")
        }
        return sb.toString()
    }

    fun updateText(context: Context, cfg: Config, loc: Location): String {
        val who = cfg.senderName.ifBlank { "Botão vermelho" }
        return "$who, localização atual: ${describe(context, loc)}"
    }

    /** Link do Google Maps + precisão, e a hora se a posição não for de agora. */
    private fun describe(context: Context, loc: Location): String {
        val link = String.format(
            Locale.US, "https://maps.google.com/?q=%.6f,%.6f", loc.latitude, loc.longitude
        )
        val extras = mutableListOf<String>()
        if (loc.hasAccuracy()) extras.add("precisão ~${loc.accuracy.toInt()} m")
        if (Locator.ageMillis(loc) > 2 * 60_000) {
            extras.add("posição das " + DateFormat.getTimeFormat(context).format(Date(loc.time)))
        }
        return if (extras.isEmpty()) link else "$link (${extras.joinToString(", ")})"
    }

    fun has(context: Context, permission: String) =
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}
