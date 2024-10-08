package com.yeager.trelloandroidwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.widget.RemoteViews

class CardListWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            deleteState(context, appWidgetId)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val cardUrl: String? = intent.getStringExtra(Intent.EXTRA_TEXT)
            val viewIntent = Intent(Intent.ACTION_VIEW, Uri.parse(cardUrl))
            viewIntent.setFlags(FLAG_ACTIVITY_NEW_TASK)
            context?.startActivity(viewIntent)
        }
        super.onReceive(context, intent)
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val views = RemoteViews(context.packageName, R.layout.card_list_widget).apply {
        val intent = Intent(context, CardListService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
        }

        setRemoteAdapter(R.id.card_list, intent)
    }

    val toastPendingIntent: PendingIntent = Intent(
        context,
        CardListWidget::class.java
    ).run {
        action = Intent.ACTION_VIEW
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))

        PendingIntent.getBroadcast(context, 0, this, PendingIntent.FLAG_MUTABLE)
    }
    views.setPendingIntentTemplate(R.id.card_list, toastPendingIntent)

    appWidgetManager.updateAppWidget(appWidgetId, views)
}