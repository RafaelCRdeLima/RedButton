package br.botaovermelho

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.app.Activity

/** Configuração feita uma vez, por quem instala o app no celular dela. */
class SettingsActivity : Activity() {

    private lateinit var contactName: EditText
    private lateinit var number: EditText
    private lateinit var senderName: EditText
    private lateinit var sendLocation: CheckBox
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        contactName = findViewById(R.id.contact_name)
        number = findViewById(R.id.number)
        senderName = findViewById(R.id.sender_name)
        sendLocation = findViewById(R.id.send_location)
        status = findViewById(R.id.status)

        val cfg = Config(this)
        if (savedInstanceState == null) {
            contactName.setText(cfg.contactName)
            number.setText(cfg.rawNumber)
            senderName.setText(cfg.senderName)
            sendLocation.isChecked = cfg.sendLocation
        }

        findViewById<Button>(R.id.save).setOnClickListener { save(askPermissions = true) }
        findViewById<Button>(R.id.test_sms).setOnClickListener { testSms() }
        findViewById<Button>(R.id.add_widget).setOnClickListener { addWidget() }
        findViewById<Button>(R.id.app_settings).setOnClickListener {
            startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
            )
        }
        val keep = findViewById<Button>(R.id.keep_permissions)
        if (Build.VERSION.SDK_INT >= 30) {
            keep.setOnClickListener {
                startActivity(
                    Intent(Intent.ACTION_AUTO_REVOKE_PERMISSIONS, Uri.fromParts("package", packageName, null))
                )
            }
        } else {
            keep.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    override fun onPause() {
        super.onPause()
        save(askPermissions = false)
    }

    private fun save(askPermissions: Boolean) {
        val cfg = Config(this)
        cfg.contactName = contactName.text.toString()
        cfg.rawNumber = number.text.toString()
        cfg.senderName = senderName.text.toString()
        cfg.sendLocation = sendLocation.isChecked
        SosWidget.updateAll(this)
        if (!askPermissions) return

        if (!cfg.isConfigured) {
            Toast.makeText(this, "Digite o número com DDD.", Toast.LENGTH_LONG).show()
            return
        }
        val missing = Permissions.missing(this)
        if (missing.isNotEmpty()) {
            requestPermissions(missing.toTypedArray(), REQ_PERMS)
        } else {
            Toast.makeText(this, "Salvo. Número: ${cfg.number}", Toast.LENGTH_LONG).show()
        }
        refreshStatus()
    }

    override fun onRequestPermissionsResult(code: Int, perms: Array<out String>, results: IntArray) {
        super.onRequestPermissionsResult(code, perms, results)
        refreshStatus()
        val missing = Permissions.missing(this)
        if (missing.isNotEmpty()) {
            Toast.makeText(
                this,
                "Ainda falta: ${Permissions.describe(missing)}. Use \"Abrir permissões do app\".",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun refreshStatus() {
        val cfg = Config(this)
        val lines = mutableListOf<String>()
        lines += if (cfg.isConfigured) "✔ Número: ${cfg.number}" else "✘ Falta o número"
        val missing = Permissions.describe(Permissions.missing(this))
        fun line(ok: Boolean, text: String) = (if (ok) "✔ " else "✘ ") + text
        lines += line(!missing.contains("SMS"), "Mandar SMS")
        lines += line(!missing.contains("ligação"), "Fazer a ligação")
        if (cfg.sendLocation) lines += line(Locator.hasPermission(this), "Localização")
        if (Build.VERSION.SDK_INT >= 30) {
            lines += line(
                packageManager.isAutoRevokeWhitelisted,
                "Não perder as permissões se o app ficar meses sem uso"
            )
        }
        status.text = lines.joinToString("\n")
    }

    private fun testSms() {
        save(askPermissions = false)
        val cfg = Config(this)
        if (!cfg.isConfigured) {
            Toast.makeText(this, "Digite o número com DDD.", Toast.LENGTH_LONG).show()
            return
        }
        val loc = if (cfg.sendLocation) Locator.lastKnown(this) else null
        val text = "TESTE do Botão Vermelho (não é emergência).\n" +
            Emergency.alertText(this, cfg, loc)
        val ok = Emergency.sendSms(this, cfg.number, text)
        Toast.makeText(
            this,
            if (ok) "Mensagem de teste enviada para ${cfg.number}" else "Não deu: falta a permissão de SMS?",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun addWidget() {
        save(askPermissions = false)
        val manager = AppWidgetManager.getInstance(this)
        if (Build.VERSION.SDK_INT >= 26 && manager.isRequestPinAppWidgetSupported) {
            manager.requestPinAppWidget(ComponentName(this, SosWidget::class.java), null, null)
        } else {
            Toast.makeText(
                this,
                "Segure o dedo num espaço vazio da tela inicial → Widgets → Botão Vermelho",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    companion object {
        private const val REQ_PERMS = 1
    }
}
