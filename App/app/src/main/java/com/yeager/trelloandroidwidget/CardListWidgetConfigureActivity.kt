package com.yeager.trelloandroidwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.yeager.trelloandroidwidget.databinding.CardListWidgetConfigureBinding
import com.yeager.trelloandroidwidget.trello.AuthorizationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * The configuration screen for the [CardListWidget] AppWidget.
 */
class CardListWidgetConfigureActivity : Activity() {
    private val authorizationService = AuthorizationService()
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private val onStartAuthClicked = View.OnClickListener {
        authorizationService.openAuthPage(this)
    }

    private val onSaveClicked = View.OnClickListener {
        val token = binding.userTokenText.text.toString()
        CoroutineScope(Dispatchers.IO).launch {
            authorizationService.saveToken(this@CardListWidgetConfigureActivity, token)
            finish()
        }
    }
    private lateinit var binding: CardListWidgetConfigureBinding

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        binding = CardListWidgetConfigureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val startAuthButton = binding.startAuthButton
        val saveTokenButton = binding.saveTokenButton

        runBlocking {
            launch {
                val token = authorizationService.loadToken(this@CardListWidgetConfigureActivity)
                if (token == null) {
                    startAuthButton.setOnClickListener(onStartAuthClicked)
                    saveTokenButton.setOnClickListener(onSaveClicked)
                } else {
                    startAuthButton.visibility = View.GONE
                    saveTokenButton.visibility = View.GONE
                    binding.userTokenText.visibility = View.GONE
                }
            }
        }

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
    }
}

private const val PREFS_NAME = "com.yeager.trelloandroidwidget.CardListWidget"
private const val PREF_PREFIX_KEY = "appwidget_"

// Read the prefix from the SharedPreferences object for this widget.
// If there is no preference saved, get the default from a resource
internal fun loadTitlePref(context: Context, appWidgetId: Int): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    val titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)
    return titleValue ?: context.getString(R.string.appwidget_text)
}

internal fun deleteTitlePref(context: Context, appWidgetId: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.remove(PREF_PREFIX_KEY + appWidgetId)
    prefs.apply()
}