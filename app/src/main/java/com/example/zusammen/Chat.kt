package com.example.zusammen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zusammen.databinding.ActivityChatBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

private val TELEGRAM_BOT_TOKEN = "7873959277:AAG3BbnMXRoWkflKPVVPMtDlPWqRVdBxx30"
private val MANAGER_CHAT_ID = "986649121"

class Chat : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var messagesAdapter: MessagesAdapter
    private val messagesList: MutableList<ChatMessage> = mutableListOf()
    private val client = OkHttpClient()
    private var lastUpdateId = 0
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 3000L



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_chat // Выделяем текущий пункт (Главная)

        // Обработчик нажатий в нижнем меню
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_calendar -> {
                    startActivity(Intent(this, CalendActivity::class.java))
                    overridePendingTransition(0, 0) // Убираем анимацию перехода
                    true
                }
                R.id.nav_chat -> {
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, Profile::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }

        setupRecyclerView()
        setupSendButton()
    }

    override fun onResume() {
        super.onResume()
        startCheckingUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopCheckingUpdates()
    }

    private fun startCheckingUpdates() {
        handler.postDelayed(updateChecker, updateInterval)
    }

    private fun stopCheckingUpdates() {
        handler.removeCallbacks(updateChecker)
    }

    private val updateChecker = object : Runnable {
        override fun run() {
            checkForNewMessages()
            handler.postDelayed(this, updateInterval)
        }
    }

    private fun checkForNewMessages() {
        val url = "https://api.telegram.org/bot$TELEGRAM_BOT_TOKEN/getUpdates?offset=${lastUpdateId + 1}"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TelegramAPI", "Update check failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val json = response.body?.string() ?: return
                    val updates = JSONObject(json).getJSONArray("result")

                    for (i in 0 until updates.length()) {
                        val update = updates.getJSONObject(i)
                        lastUpdateId = update.getInt("update_id")

                        if (update.has("message")) {
                            val message = update.getJSONObject("message")
                            val chatId = message.getJSONObject("chat").getString("id")

                            if (chatId == MANAGER_CHAT_ID && message.has("text")) {
                                val text = message.getString("text")
                                runOnUiThread {
                                    addMessageToChat(text, false)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("TelegramAPI", "Error parsing updates", e)
                }
            }
        })
    }

    private fun setupRecyclerView() {
        messagesAdapter = MessagesAdapter(messagesList)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@Chat)
            adapter = messagesAdapter
        }
    }

    private fun setupSendButton() {
        // Обработчик для иконки отправки
        binding.messageInputLayout.setEndIconOnClickListener {
            sendMessage()
        }

        // Обработчик для кнопки отправки на клавиатуре
        binding.etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }
    }

    private fun sendMessage() {
        val messageText = binding.etMessage.text.toString().trim()
        if (messageText.isNotEmpty()) {
            sendMessageToTelegram(messageText)
            addMessageToChat(messageText, true)
            binding.etMessage.setText("")
        }
    }

    private fun addMessageToChat(text: String, isUserMessage: Boolean) {
        val message = ChatMessage(
            text = text,
            isUserMessage = isUserMessage
        )
        messagesList.add(message)
        messagesAdapter.notifyItemInserted(messagesList.size - 1)
        binding.recyclerView.scrollToPosition(messagesList.size - 1)
    }

    private fun sendMessageToTelegram(text: String) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val json = """
            {
                "chat_id": $MANAGER_CHAT_ID,
                "text": "$text"
            }
        """.trimIndent()

        val body = json.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://api.telegram.org/bot$TELEGRAM_BOT_TOKEN/sendMessage")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TelegramAPI", "Message send failed", e)
                runOnUiThread {
                    Toast.makeText(this@Chat, "Failed to send message", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e("TelegramAPI", "Message send failed: ${response.code}")
                }
            }
        })
    }
}