package com.yeager.trelloandroidwidget

import android.content.Context
import com.yeager.trelloandroidwidget.trello.TrelloList
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val PREFS_NAME = "com.yeager.trelloandroidwidget.CardListWidget"
private const val PREF_PREFIX_KEY = "appwidget_"

@Serializable
data class SaveState(
    var boardName: String = "",
    val lists: MutableList<TrelloList> = mutableListOf(),
    var showDueDates: Boolean = true,
    var showListNames: Boolean = true,
)

internal fun saveState(context: Context, appWidgetId: Int, state: SaveState) {
    val stateData = Json.encodeToString(state)
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.putString(PREF_PREFIX_KEY + appWidgetId, stateData)
    prefs.apply()
}

internal fun loadState(context: Context, appWidgetId: Int): SaveState {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    val stateData = prefs.getString(PREF_PREFIX_KEY + appWidgetId, "")!!
    return if (stateData == "") SaveState() else Json.decodeFromString<SaveState>(stateData)
}


internal fun deleteState(context: Context, appWidgetId: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.remove(PREF_PREFIX_KEY + appWidgetId)
    prefs.apply()
}