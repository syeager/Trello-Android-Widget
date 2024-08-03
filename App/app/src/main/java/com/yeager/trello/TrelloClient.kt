package com.yeager.trello

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.collections.List

private const val TRELLO_API_ROOT = "https://api.trello.com/1"

class TrelloClient(key: String, token: String) :
    CallGetAllBoards,
    CallGetListsInBoard,
    CallGetCardsInList {
    private val json: Json = Json { ignoreUnknownKeys = true }

    private val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        defaultRequest {
            headers {
                append(HttpHeaders.Accept, "application/json")
            }
            url {
                parameters.append("key", key)
                parameters.append("token", token)
            }
        }
    }

    private suspend inline fun <reified T> getList(route: String): List<T> {
        val response = client.get("$TRELLO_API_ROOT/$route")
        val value: List<T> = json.decodeFromString(response.body())
        return value
    }

    override suspend fun getAllBoards() = getList<TrelloBoard>("boards")

    override suspend fun getAllListsInBoard(boardId: String) =
        getList<TrelloList>("boards/$boardId/lists")

    override suspend fun getAllCardsInList(listId: String) =
        getList<TrelloCard>("lists/$listId/cards")
}

interface CallGetAllBoards {
    suspend fun getAllBoards(): List<TrelloBoard>
}

interface CallGetListsInBoard {
    suspend fun getAllListsInBoard(boardId: String): List<TrelloList>
}

interface CallGetCardsInList {
    suspend fun getAllCardsInList(listId: String): List<TrelloCard>
}