package br.botaovermelho

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/** Botão vermelho na tela inicial: um toque e já pede ajuda. */
class SosWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        update(context, manager, ids)
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, SosWidget::class.java))
            if (ids.isNotEmpty()) update(context, manager, ids)
        }

        private fun update(context: Context, manager: AppWidgetManager, ids: IntArray) {
            val cfg = Config(context)
            val tap = PendingIntent.getActivity(
                context, 0,
                Intent(context, TriggerActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val views = RemoteViews(context.packageName, R.layout.widget_sos).apply {
                setTextViewText(
                    R.id.widget_sub,
                    if (cfg.isConfigured) "Ligar para ${cfg.contactLabel}" else "Falta configurar"
                )
                setOnClickPendingIntent(R.id.widget_root, tap)
            }
            manager.updateAppWidget(ids, views)
        }
    }
}
