package com.example.zusammen

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.example.zusammen.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val allPerformers = mutableListOf<Performer>()

    // Модель данных исполнителя
    data class Performer(
        val id: Int,
        val name: String,
        val specialty: String,
        val description: String,
        val imageRes: Int,
        val price: Int,
        val category: String,
        val buttonId: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация данных
        initializePerformers()

        // Настройка UI компонентов
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_home // Выделяем текущий пункт (Главная)

        // Обработчик нажатий в нижнем меню
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_calendar -> {
                    startActivity(Intent(this, CalendActivity::class.java))
                    overridePendingTransition(0, 0) // Убираем анимацию перехода
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

        setupCategoryButtons()
        setupPerformerButtons()
        setupSearchView()
        setupCartButton()
    }

    // Инициализация списка исполнителей
    private fun initializePerformers() {
        allPerformers.addAll(listOf(
            Performer(
                id = 1,
                name = "Иван Петров",
                specialty = "Фотограф",
                description = "Опыт работы: 7 лет\nСвадебные фотосессии\nПолный день съемки",
                imageRes = R.drawable.performer1,
                price = 15000,
                category = "Фотографы",
                buttonId = R.id.performer1_button
            ),
            Performer(
                id = 2,
                name = "Анна Смирнова",
                specialty = "Ведущая",
                description = "Опыт работы: 5 лет\nИндивидуальный сценарий\nМузыкальное сопровождение",
                imageRes = R.drawable.performer2,
                price = 20000,
                category = "Ведущие",
                buttonId = R.id.performer2_button
            ),
            Performer(
                id = 3,
                name = "Сергей Иванов",
                specialty = "Музыкант",
                description = "Опыт работы: 2 года\nМузыкальное сопровождение",
                imageRes = R.drawable.performer3,
                price = 20000,
                category = "Музыканты",
                buttonId = R.id.performer3_button
            )
        ))
    }

    // Настройка нижней навигации


    // Настройка кнопок категорий
    private fun setupCategoryButtons() {
        binding.apply {
            btnPhotographers.setOnClickListener { startCategoryActivity("Фотографы") }
            btnRestaurants.setOnClickListener { startCategoryActivity("Рестораны") }
            btnCars.setOnClickListener { startCategoryActivity("Транспорт") }
            btnPeople.setOnClickListener { startCategoryActivity("Ведущие") }
            btnDecoration.setOnClickListener { startCategoryActivity("Декор") }
            btnDress.setOnClickListener { startCategoryActivity("Платья") }
        }
    }

    // Настройка кнопок исполнителей
    private fun setupPerformerButtons() {
        binding.apply {
            performer1Button.setOnClickListener { openPerformerDetails(allPerformers[0]) }
            performer2Button.setOnClickListener { openPerformerDetails(allPerformers[1]) }
        }
        findViewById<Button>(R.id.performer3_button).setOnClickListener {
            openPerformerDetails(allPerformers[2])
        }
    }

    // Настройка поиска
    private fun setupSearchView() {
        binding.searchView.apply {
            queryHint = "Поиск исполнителей..."
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { performSearch(it) }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let { performSearch(it) }
                    return true
                }
            })
        }
    }

    private fun setupCartButton() {
        binding.cartButton.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
    }

    // Логика поиска
    private fun performSearch(query: String) {
        val searchText = query.trim().lowercase(Locale.getDefault())

        if (searchText.isEmpty()) {
            showAllPerformers()
            return
        }

        val filtered = allPerformers.filter {
            it.name.lowercase(Locale.getDefault()).contains(searchText) ||
                    it.specialty.lowercase(Locale.getDefault()).contains(searchText) ||
                    it.category.lowercase(Locale.getDefault()).contains(searchText)
        }

        updatePerformersVisibility(filtered)

        if (filtered.isEmpty()) {
            Toast.makeText(this, "Исполнители не найдены", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAllPerformers() {
        allPerformers.forEach {
            findViewById<Button>(it.buttonId).visibility = View.VISIBLE
        }
    }

    private fun updatePerformersVisibility(visiblePerformers: List<Performer>) {
        allPerformers.forEach { performer ->
            findViewById<Button>(performer.buttonId).visibility =
                if (visiblePerformers.contains(performer)) View.VISIBLE else View.GONE
        }
    }

    private fun startCategoryActivity(categoryName: String) {
        startActivity(Intent(this, Category::class.java).apply {
            putExtra("category", categoryName)
        })
    }

    private fun openPerformerDetails(performer: Performer) {
        startActivity(Intent(this, PerformerDetailActivity::class.java).apply {
            putExtra("name", performer.name)
            putExtra("specialty", performer.specialty)
            putExtra("description", performer.description)
            putExtra("image", performer.imageRes)
            putExtra("price", performer.price)
            putExtra("category", performer.category)
        })
    }
}