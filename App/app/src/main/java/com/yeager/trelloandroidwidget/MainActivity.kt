package com.yeager.trelloandroidwidget

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.yeager.trelloandroidwidget.databinding.ActivityMainBinding
import com.yeager.trelloandroidwidget.trello.AuthorizationService
import com.yeager.trelloandroidwidget.trello.createTrelloClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private enum class State {
    LoggedIn,
    LoggedOut,
}

class MainActivity : AppCompatActivity() {
    private val authorizationService = AuthorizationService()
    private lateinit var binding: ActivityMainBinding
    private val context: Context = this

    private val onStartAuthClicked = View.OnClickListener {
        authorizationService.openAuthPage(this)
    }

    private val onTokenSaveClicked = View.OnClickListener {
        val token = binding.userTokenText.text.toString()
        CoroutineScope(Dispatchers.IO).launch {
            authorizationService.saveToken(context, token)

            val resultValue = Intent()
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.logOutButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                authorizationService.clearToken(context)
            }
            updateState(State.LoggedOut)
        }

        binding.startAuthButton.setOnClickListener(onStartAuthClicked)
        binding.saveTokenButton.setOnClickListener(onTokenSaveClicked)
        binding.userTokenText.doOnTextChanged { _, _, _, _ -> renderLoggedOut() }

        lifecycleScope.launch {
            val token = authorizationService.loadToken(context)
            val state = if (token?.isNotEmpty() == true) State.LoggedIn else State.LoggedOut
            updateState(state)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun updateState(state: State) {
        when (state) {
            State.LoggedIn -> renderLoggedIn()
            State.LoggedOut -> renderLoggedOut()
        }
    }

    private fun renderLoggedIn() {
        binding.authLayout.visibility = View.GONE
        binding.memberLayout.visibility = View.VISIBLE

        lifecycleScope.launch {
            val member = createTrelloClient(context, authorizationService).getMember()

            binding.authLayout.visibility = View.GONE
            binding.memberLayout.visibility = View.VISIBLE

            binding.memberName.text = member.username
        }
    }

    private fun renderLoggedOut() {
        binding.authLayout.visibility = View.VISIBLE
        binding.memberLayout.visibility = View.GONE

        val hasToken = binding.userTokenText.text.isNotEmpty()
        binding.startAuthButton.visibility = if (hasToken) View.GONE else View.VISIBLE
        binding.saveTokenButton.visibility = if (!hasToken) View.GONE else View.VISIBLE
    }
}
