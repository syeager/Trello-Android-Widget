package com.yeager.trelloandroidwidget.trello

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

private val TOKEN = stringPreferencesKey("token")

class AuthorizationService {
    fun openAuthPage(context: Context, key: String) {
        val url = "https://api.trello.com/1/authorize?key=$key"
        val i = Intent(Intent.ACTION_VIEW)
        i.setData(Uri.parse(url))
        startActivity(context, i, null)
    }

    suspend fun saveToken(context: Context, token: String) {
        context.dataStore.edit { settings ->
            settings[TOKEN] = token
        }
    }

    suspend fun loadToken(context: Context): String? {
        var token: String? = null
        context.dataStore.edit { settings ->
            token = settings[TOKEN]
        }

        return token
    }
}