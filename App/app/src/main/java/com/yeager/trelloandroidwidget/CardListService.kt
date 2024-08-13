package com.yeager.trelloandroidwidget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.yeager.trelloandroidwidget.trello.AuthorizationService
import com.yeager.trelloandroidwidget.trello.TrelloClient
import com.yeager.trelloandroidwidget.trello.createTrelloClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class CardListService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent) =
        CardListViewsViewsFactory(this.applicationContext, intent)
}

class CardListViewsViewsFactory(
    private val context: Context,
    intent: Intent
) : RemoteViewsService.RemoteViewsFactory {
    private val cards: ArrayList<CardViewModel> = arrayListOf()
    private lateinit var trelloClient: TrelloClient
    private var dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS]'Z'")

    private val appWidgetId: Int =
        intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

    private var isLoading = false

    override fun onCreate() {
        runBlocking {
            trelloClient = createTrelloClient(context, AuthorizationService())
        }
    }

    override fun onDataSetChanged() {
        val state = loadState(context, appWidgetId)

        state.lists.forEach { list ->
            runBlocking {
                launch {
                    val listCards = trelloClient.getAllCardsInList(list.id)
                    cards.addAll(listCards
                        .filter { c -> c.subscribed }
                        .map { c -> CardViewModel(
                            c.name,
                            Uri.parse(c.url),
                            if (c.due != null) ZonedDateTime.parse(c.due).toLocalDate() else null,
                            list.name
                        ) })
                }
            }
        }

        cards.sortBy { it.cardName }
        cards.sortBy { it.dueDate }
    }

    override fun onDestroy() = cards.clear()

    override fun getCount(): Int = cards.count()

    override fun getViewAt(p0: Int): RemoteViews {
        val cardView = RemoteViews(context.packageName, R.layout.card)
        cardView.setTextViewText(R.id.label, cards[p0].toString())
        return cardView
    }

    override fun getLoadingView() = null

    override fun getViewTypeCount() = 1

    override fun getItemId(p0: Int) = p0.toLong()

    override fun hasStableIds() = true

}