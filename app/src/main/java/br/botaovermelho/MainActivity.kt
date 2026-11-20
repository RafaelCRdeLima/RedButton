package br.botaovermelho

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.TextView
import android.widget.Toast

/** A tela que ela vê: um botão vermelho enorme. */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.sos_button).setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            Emergency.trigger(this)
        }

        // Configurações só com o dedo segurado, para ela não abrir sem querer.
        val settings = findViewById<View>(R.id.settings_link)
        settings.setOnClickListener {
            Toast.makeText(this, "Segure o dedo para abrir as configurações", Toast.LENGTH_SHORT).show()
        }
        settings.setOnLongClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }

        if (savedInstanceState == null && !Config(this).isConfigured) {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val cfg = Config(this)
        findViewById<TextView>(R.id.info).text =
            "Liga e manda mensagem para ${cfg.contactLabel}"

        val status = findViewById<TextView>(R.id.status)
        val missing = Permissions.missing(this)
        status.text = when {
            !cfg.isConfigured -> "Falta configurar o número."
            missing.isNotEmpty() ->
                "Atenção: faltam permissões (${Permissions.describe(missing)}). " +
                    "Peça para ${cfg.contactLabel} abrir as configurações."
            cfg.lastTrigger > 0 -> "Último pedido de ajuda: " + DateUtils.formatDateTime(
                this, cfg.lastTrigger,
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME
            )
            else -> ""
        }
        status.setTextColor(
            if (!cfg.isConfigured || missing.isNotEmpty()) 0xFFC62828.toInt() else 0xFF555555.toInt()
        )
    }
}
