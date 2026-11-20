package br.botaovermelho

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper

/**
 * Roda por até [TIMEOUT_MS] depois do botão, já com a ligação em andamento,
 * para pegar uma posição atual e mandar numa segunda mensagem.
 * É um serviço em primeiro plano porque, com o app atrás da tela da
 * ligação, o Android só entrega a localização a um serviço assim.
 */
class LocationService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var lm: LocationManager? = null
    private var best: Location? = null
    private var finished = false

    private val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (Locator.better(location, best)) best = location
            if (location.hasAccuracy() && location.accuracy <= GOOD_ACCURACY_M) finish()
        }
        // Precisam existir: antes do Android 10 não têm implementação padrão.
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!goForeground() || !Locator.hasPermission(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        val lm = getSystemService(LocationManager::class.java)
        this.lm = lm
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { lm.isProviderEnabled(it) }
        if (providers.isEmpty()) {          // localização do celular desligada
            stopSelf()
            return START_NOT_STICKY
        }
        for (p in providers) {
            try {
                lm.requestLocationUpdates(p, 1000L, 0f, listener, Looper.getMainLooper())
            } catch (_: Exception) {
            }
        }
        handler.postDelayed({ finish() }, TIMEOUT_MS)
        return START_NOT_STICKY
    }

    private fun finish() {
        if (finished) return
        finished = true
        handler.removeCallbacksAndMessages(null)
        lm?.removeUpdates(listener)
        best?.let { loc ->
            val cfg = Config(this)
            Emergency.sendSms(this, cfg.number, Emergency.updateText(this, cfg, loc))
        }
        stopSelf()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        lm?.removeUpdates(listener)
        super.onDestroy()
    }

    private fun goForeground(): Boolean {
        val nm = getSystemService(NotificationManager::class.java)
        val builder = if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL, "Pedido de ajuda", NotificationManager.IMPORTANCE_LOW)
            )
            Notification.Builder(this, CHANNEL)
        } else {
            @Suppress("DEPRECATION") Notification.Builder(this)
        }
        val n = builder
            .setSmallIcon(R.drawable.ic_stat_sos)
            .setContentTitle("Pedido de ajuda enviado")
            .setContentText("Mandando a localização…")
            .setOngoing(true)
            .build()
        return try {
            if (Build.VERSION.SDK_INT >= 29) {
                startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
            } else {
                startForeground(NOTIF_ID, n)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val CHANNEL = "sos"
        private const val NOTIF_ID = 1
        private const val TIMEOUT_MS = 45_000L
        private const val GOOD_ACCURACY_M = 30f

        fun start(context: Context) {
            val i = Intent(context, LocationService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(i)
                else context.startService(i)
            } catch (_: Exception) {
                // Sem a segunda mensagem; a primeira e a ligação já foram.
            }
        }
    }
}
