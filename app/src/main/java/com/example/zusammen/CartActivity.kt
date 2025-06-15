package com.example.zusammen

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.zusammen.databinding.ActivityCartBinding
import com.example.zusammen.databinding.ItemCartBinding

class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Настройка edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Настройка Toolbar
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Обновление данных корзины
        updateCart()

        // Обработчик кнопки оформления заказа
        binding.checkoutButton.setOnClickListener {
            if (Cart.getItems().isNotEmpty()) {
                showCheckoutDialog()
            } else {
                Toast.makeText(this, "Корзина пуста", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateCart() {
        binding.cartContainer.removeAllViews()
        val items = Cart.getItems()

        if (items.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "Корзина пуста"
                textSize = 18f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 32.dpToPx(), 0, 0)
            }
            binding.cartContainer.addView(emptyText)
            binding.checkoutButton.isEnabled = false
        } else {
            items.forEach { item ->
                addCartItemView(item)
            }
            binding.checkoutButton.isEnabled = true
        }

        binding.totalText.text = "Итого: ${Cart.calculateTotal()}"
    }

    private fun addCartItemView(item: Cart.CartItem) {
        val itemBinding = ItemCartBinding.inflate(
            LayoutInflater.from(this),
            binding.cartContainer,
            false
        )

        with(itemBinding) {
            cartItemName.text = item.name
            cartItemDescription.text = item.description
            cartItemPrice.text = item.price
            cartItemImage.setImageResource(item.imageRes)

            // Обработчик удаления элемента
            cartItemDelete.setOnClickListener {
                showRemoveItemDialog(item)
            }
        }

        binding.cartContainer.addView(itemBinding.root)
    }

    private fun showRemoveItemDialog(item: Cart.CartItem) {
        AlertDialog.Builder(this)
            .setTitle("Удалить из корзины")
            .setMessage("Вы уверены, что хотите удалить ${item.name} из корзины?")
            .setPositiveButton("Удалить") { _, _ ->
                Cart.removeItem(item)
                updateCart()
                Toast.makeText(this, "${item.name} удален", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showCheckoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Оформление заказа")
            .setMessage("Подтвердить заказ на сумму ${Cart.calculateTotal()}?")
            .setPositiveButton("Подтвердить") { _, _ ->
                Cart.clearCart()
                updateCart()
                Toast.makeText(this, "Заказ оформлен!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        updateCart()
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}