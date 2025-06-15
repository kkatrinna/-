package com.example.zusammen

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PerformerDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_performer_detail)

        // Получаем данные из Intent
        val name = intent.getStringExtra("name") ?: ""
        val specialty = intent.getStringExtra("specialty") ?: ""
        val rating = intent.getFloatExtra("rating", 0f)
        val description = intent.getStringExtra("description") ?: ""
        val imageRes = intent.getIntExtra("image", 0)
        val price = intent.getIntExtra("price", 0)
        val category = intent.getStringExtra("category") ?: ""

        // Устанавливаем данные в UI
        findViewById<TextView>(R.id.detail_name).text = name
        findViewById<TextView>(R.id.detail_specialty).text = specialty
        findViewById<TextView>(R.id.detail_description).text = description
        findViewById<ImageView>(R.id.detail_image).setImageResource(imageRes)
        findViewById<TextView>(R.id.detail_price).text = "Цена: $price ₽"

        // Кнопка "Назад"
        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            finish()
        }

        // Кнопка "Забронировать" - добавление в корзину
        findViewById<Button>(R.id.book_button).setOnClickListener {
            // Создаем объект для корзины
            val cartItem = Cart.CartItem(// Уникальный идентификатор
                name = "$name ($specialty)",
                description = description,
                price = "$price ₽",
                imageRes = imageRes,
                category = category
            )


                // Добавляем в корзину
                Cart.addItem(cartItem)
                Toast.makeText(this, "$name добавлен в корзину", Toast.LENGTH_SHORT).show()


            finish()
        }
    }
}