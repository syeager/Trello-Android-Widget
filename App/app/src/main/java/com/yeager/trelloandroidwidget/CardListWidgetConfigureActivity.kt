package com.yeager.trelloandroidwidget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.yeager.trelloandroidwidget.databinding.CardListWidgetConfigureBinding
import com.yeager.trelloandroidwidget.trello.AuthorizationService
import com.yeager.trelloandroidwidget.trello.createTrelloClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The configuration screen for the [CardListWidget] AppWidget.
 */
class CardListWidgetConfigureActivity : AppCompatActivity() {
    private val authorizationService = AuthorizationService()
    private val state = SaveState()
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private val context: Context = this

    private val onListsSaveClicked = View.OnClickListener {
        saveState(context, appWidgetId, state)

        val appWidgetManager = AppWidgetManager.getInstance(context)
        updateAppWidget(context, appWidgetManager, appWidgetId)

        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        val binding = CardListWidgetConfigureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            val token = authorizationService.loadToken(context)
            if (token == null) {
                // TODO: Change to main activity.
            } else {
                binding.saveListsButton.setOnClickListener(onListsSaveClicked)

                val trelloClient = createTrelloClient(context, authorizationService)
                val boards = withContext(Dispatchers.IO) {
                    trelloClient.getAllBoards()
                }

                val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, boards.map { it.name })
                val boardSpinner = binding.boardSpinner
                boardSpinner.adapter = adapter
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                boardSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {}

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        println("selected ${boards[position]}")
                        state.boardName = boards[position].name
                        state.lists.clear()

                        lifecycleScope.launch {
                            println("fetching lists")
                            val lists = trelloClient.getAllListsInBoard(boards[position].id)
                            val listLayout = binding.listLayout
                            listLayout.removeAllViews()
                            lists.forEach { list ->
                                val checkbox = CheckBox(context)
                                checkbox.text = list.name
                                checkbox.setOnCheckedChangeListener { _, b ->
                                    if (b) state.lists.add(list)
                                    else state.lists.remove(list)
                                }
                                listLayout.addView(checkbox)
                            }
                        }
                    }
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
