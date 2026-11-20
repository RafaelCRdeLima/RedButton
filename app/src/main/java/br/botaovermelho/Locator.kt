package br.botaovermelho

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.SystemClock

object Locator {

    fun hasPermission(context: Context) =
        Emergency.has(context, Manifest.permission.ACCESS_FINE_LOCATION) ||
            Emergency.has(context, Manifest.permission.ACCESS_COARSE_LOCATION)

    /** A posição mais recente que o celular já tem guardada (instantâneo, pode ser velha). */
    @SuppressLint("MissingPermission")
    fun lastKnown(context: Context): Location? {
        if (!hasPermission(context)) return null
        val lm = context.getSystemService(LocationManager::class.java) ?: return null
        val all = lm.allProviders
            .mapNotNull { p -> try { lm.getLastKnownLocation(p) } catch (e: Exception) { null } }
        // Entre as dos últimos 2 minutos, a mais precisa; se não houver, a mais nova.
        val recent = all.filter { ageMillis(it) < 2 * 60_000 }
        return recent.reduceOrNull { a, b -> if (better(a, b)) a else b }
            ?: all.minByOrNull { ageMillis(it) }
    }

    fun ageMillis(loc: Location): Long =
        (SystemClock.elapsedRealtimeNanos() - loc.elapsedRealtimeNanos) / 1_000_000

    /** De até 2 minutos atrás e com precisão de 50 m ou melhor: não precisa buscar outra. */
    fun isGood(loc: Location?) =
        loc != null && ageMillis(loc) < 2 * 60_000 && loc.hasAccuracy() && loc.accuracy <= 50f

    /** Fica com a mais precisa; empate (ou sem precisão) fica com a mais nova. */
    fun better(a: Location, b: Location?): Boolean {
        if (b == null) return true
        val accA = if (a.hasAccuracy()) a.accuracy else Float.MAX_VALUE
        val accB = if (b.hasAccuracy()) b.accuracy else Float.MAX_VALUE
        return accA < accB || (accA == accB && a.elapsedRealtimeNanos > b.elapsedRealtimeNanos)
    }
}
