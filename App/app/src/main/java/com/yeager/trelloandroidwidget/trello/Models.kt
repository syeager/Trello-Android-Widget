package com.yeager.trelloandroidwidget.trello

import kotlinx.serialization.Serializable

@Serializable
data class TrelloBoard(val id: String, val name: String)

@Serializable
data class TrelloList(val id: String, val name: String)

@Serializable
data class TrelloCard(
    val id: String,
    val name: String,
    val url: String,
    val due: String?,
    val subscribed: Boolean
)