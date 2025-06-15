package com.example.zusammen

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.zusammen.databinding.ActivityAddEventBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class AddEvent : AppCompatActivity() {
    private lateinit var binding: ActivityAddEventBinding
    private lateinit var eventDao: EventDao
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eventDao = AppDatabase.getDatabase(this).eventDao()

        // Получаем выбранную дату из интента
        val selectedDateMillis = intent.getLongExtra("SELECTED_DATE", System.currentTimeMillis())
        calendar.timeInMillis = selectedDateMillis

        setupToolbar()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        binding.etEventTime.setOnClickListener {
            showTimePicker()
        }

        binding.btnSaveNote.setOnClickListener {
            saveEvent()
        }
    }

    private fun showTimePicker() {
        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                binding.etEventTime.setText(
                    String.format("%02d:%02d", hourOfDay, minute)
                )
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun saveEvent() {
        val title = binding.etEventTitle.text.toString().trim()
        val description = binding.etEventContent.text.toString().trim()
        val time = binding.etEventTime.text.toString().trim()

        if (title.isEmpty()) {
            binding.etEventTitle.error = "Введите заголовок"
            return
        }

        if (time.isEmpty()) {
            Toast.makeText(this, "Укажите время события", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val event = Event(
                    title = title,
                    description = description,
                    time = time,
                    date = calendar.timeInMillis,
                    userId = getCurrentUserId()
                )

                withContext(Dispatchers.IO) {
                    eventDao.insert(event)
                }

                setResult(RESULT_OK)
                finish()
            } catch (e: Exception) {
                Toast.makeText(
                    this@AddEvent,
                    "Ошибка сохранения: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getCurrentUserId(): Int {
        return getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getInt("user_id", -1)
    }
}
