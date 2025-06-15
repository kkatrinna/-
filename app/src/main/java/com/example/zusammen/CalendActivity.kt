package com.example.zusammen

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.example.zusammen.databinding.ActivityCalendBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class CalendActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendBinding
    private lateinit var eventDao: EventDao
    private lateinit var eventsAdapter: EventsAdapter
    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eventDao = AppDatabase.getDatabase(this).eventDao()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_calendar // Выделяем текущий пункт (Главная)

        // Обработчик нажатий в нижнем меню
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0) // Убираем анимацию перехода
                    true
                }
                R.id.nav_calendar -> {
                    true
                }
                R.id.nav_chat -> {
                    startActivity(Intent(this, Chat::class.java))
                    overridePendingTransition(0, 0)
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

        setupCalendar()
        setupRecyclerView()
        setupListeners()
    }

    private fun setupCalendar() {
        binding.calendarView.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                selectedDate = eventDay.calendar
                loadEventsForSelectedDate()
            }
        })
    }



    private fun setupRecyclerView() {
        eventsAdapter = EventsAdapter(emptyList()) { event ->
            // Обработка клика на событие (например, редактирование)
            openEditEventActivity(event)
        }
        binding.eventsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CalendActivity)
            adapter = eventsAdapter
        }
    }

    private fun loadEventsForSelectedDate() {
        lifecycleScope.launch {
            try {
                val startOfDay = Calendar.getInstance().apply {
                    timeInMillis = selectedDate.timeInMillis
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val endOfDay = Calendar.getInstance().apply {
                    timeInMillis = selectedDate.timeInMillis
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis

                val events = withContext(Dispatchers.IO) {
                    eventDao.getEventsByDateRange(getCurrentUserId(), startOfDay, endOfDay)
                }
                eventsAdapter.updateEvents(events)
            } catch (e: Exception) {
                Toast.makeText(this@CalendActivity, "Ошибка загрузки событий", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.fabAddEvent.setOnClickListener {
            openAddEventActivity()
        }
    }

    private fun openAddEventActivity() {
        val intent = Intent(this, AddEvent::class.java).apply {
            // Передаем выбранную дату в миллисекундах
            putExtra("SELECTED_DATE", selectedDate.timeInMillis)
        }
        startActivityForResult(intent, ADD_EVENT_REQUEST)
    }

    private fun openEditEventActivity(event: Event) {
        Intent(this, AddEvent::class.java).apply {
            putExtra("event_id", event.id)
            putExtra("event_title", event.title)
            putExtra("event_description", event.description)
            putExtra("event_time", event.time)
            putExtra("event_date", event.date)
            startActivityForResult(this, EDIT_EVENT_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == ADD_EVENT_REQUEST || requestCode == EDIT_EVENT_REQUEST) &&
            resultCode == RESULT_OK) {
            loadEventsForSelectedDate()
        }
    }

    private fun getCurrentUserId(): Int {
        return getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getInt("user_id", -1)
    }

    companion object {
        const val ADD_EVENT_REQUEST = 1001
        const val EDIT_EVENT_REQUEST = 1002
    }
}