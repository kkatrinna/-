package com.example.zusammen

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class Category : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val category = intent.getStringExtra("category") ?: ""
        title = category


        val container = findViewById<LinearLayout>(R.id.services_container)

        when (category) {
            "Фотографы" -> {
                addServiceView(
                    container,
                    "Иван Петров",
                    "Профессиональная свадебная фотография. Работаю в классическом стиле",
                    "15000 ₽",
                    R.drawable.performer1,
                    "Фотограф"
                )
                addServiceView(
                    container,
                    "Анна Смирнова",
                    "Свадебные фотосессии в романтическом стиле",
                    "12000 ₽",
                    R.drawable.performer6,
                    "Фотограф"
                )
            }

            "Рестораны" -> {
                addServiceView(
                    container,
                    "Ресторан 'У моря'",
                    "",
                    "2500 ₽/чел",
                    R.drawable.restaurant1,
                    "Рестораны"
                )
                addServiceView(container, "La Bella", "", "3500 ₽/чел", R.drawable.restaurant2,
                    "Рестораны")
            }

            "Платья" -> {
                addServiceView(
                    container,
                    "Платье «Элен»",
                    "Кружевное",
                    "45 000 ₽",
                    R.drawable.dress2,
                    "Платья"
                )
                addServiceView(
                    container,
                    "Платье «Милана»",
                    "Минималистичное ",
                    "38 000 ₽",
                    R.drawable.dress1,
                    "Платья"
                )
            }

            "Декор" -> {
                addServiceView(
                    container,
                    "Декор «Романтика»",
                    "Цветы и драпировка",
                    "70 000 ₽",
                    R.drawable.decor1,
                    "Декор"
                )
                addServiceView(
                    container,
                    "Цветочная арка",
                    "Диаметр 3 метра",
                    "25 000 ₽",
                    R.drawable.decor2,
                    "Декор"
                )
            }

            "Транспорт" -> {
                addServiceView(
                    container,
                    "Rolls-Royce Phantom",
                    "Автомабиль на 6 часов",
                    "25 000 ₽",
                    R.drawable.car1,
                    "Транспорт"
                )
                addServiceView(
                    container,
                    " Jaguar XF",
                    "Удобный автомобиль",
                    "30 000 ₽",
                    R.drawable.car2,
                    "Транспорт"
                )
            }

            "Ведущие" -> {
                addServiceView(
                    container,
                    "Алексей Андреев",
                    "Опытный ведущий",
                    "30 000 ₽",
                    R.drawable.host1,
                    "Ведущий"
                )
                addServiceView(
                    container,
                    "Анна Смирнова",
                    "Камерные свадьбы",
                    "25 000 ₽",
                    R.drawable.performer2,
                    "Ведущий"
                )
            }
        }
    }

    private fun addServiceView(
        container: LinearLayout,
        name: String,
        description: String,
        price: String,
        imageRes: Int,
        category: String // Добавляем параметр категории
    ) {
        val view = layoutInflater.inflate(R.layout.item_service, container, false)

        view.findViewById<TextView>(R.id.service_name).text = name
        view.findViewById<TextView>(R.id.service_description).text = description
        view.findViewById<TextView>(R.id.service_price).text = price
        view.findViewById<ImageView>(R.id.service_image).setImageResource(imageRes)

        view.findViewById<Button>(R.id.add_button).setOnClickListener {
            val item = Cart.CartItem(
                name = name,
                description = description,
                price = price,
                imageRes = imageRes,
                category = category // Передаём категорию
            )
            Cart.addItem(item)
            Toast.makeText(this, "$name добавлен в корзину", Toast.LENGTH_SHORT).show()

        }

        container.addView(view)
    }

}