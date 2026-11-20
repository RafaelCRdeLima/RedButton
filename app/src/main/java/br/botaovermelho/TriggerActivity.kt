package br.botaovermelho

import android.app.Activity
import android.os.Bundle

/** Sem tela: o widget abre esta activity, que pede ajuda e fecha. */
class TriggerActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Emergency.trigger(this)
        finish()
    }
}
