package com.yeager.trelloandroidwidget.trello

import android.content.Context
import com.yeager.trelloandroidwidget.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.headers
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

suspend fun createTrelloClient(
    context: Context,
    authorizationService: AuthorizationService
): TrelloClient {
    val key = BuildConfig.TRELLO_API_KEY
    val token = authorizationService.loadToken(context)!!
    return TrelloClient(key, token)
}

class TrelloClient(key: String, token: String) {
    private val json: Json = Json { ignoreUnknownKeys = true }

    private val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
        defaultRequest {
            headers {
                append(HttpHeaders.Accept, "*/*")
                append(HttpHeaders.AcceptEncoding, "gzip, deflate, br")
                append(
                    HttpHeaders.Authorization,
                    "OAuth oath_consumer_key=\"$key\", oauth_token=\"$token\""
                )
            }
            url {
                protocol = URLProtocol.HTTPS
                host = "api.trello.com"
                path("1/")
                parameters.append("key", key)
                parameters.append("token", token)
            }
        }
    }

    private suspend inline fun <reified T> getList(
        route: String,
        vararg params: Pair<String, String>
    ): Array<T> {
        val response = client.get(route) {
            url {
                params.forEach { parameters.append(it.first, it.second) }
            }
        }
        val value = response.body<Array<T>>()
        return value
    }

    suspend fun getAllBoards() =
        getList<TrelloBoard>(
            "members/me/boards",
            "filter" to "open",
            "fields" to "id,name",
        )

    suspend fun getAllListsInBoard(boardId: String) =
        getList<TrelloList>("boards/$boardId/lists/open", "fields" to "id,name")

    suspend fun getAllCardsInList(listId: String) =
        getList<TrelloCard>("lists/$listId/cards")
}