package com.example.zusammen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.zusammen.databinding.ActivityAuthorizationBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt

class Authorization : AppCompatActivity() {
    private lateinit var binding: ActivityAuthorizationBinding
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthorizationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация базы данных
        userDao = AppDatabase.getDatabase(applicationContext).userDao()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.apply {
            btnSignIn.setOnClickListener {
                handleLogin()
            }

            tvRedirectLogin.setOnClickListener {
                navigateToRegister()
            }
        }
    }

    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        when {
            email.isEmpty() -> showToast("Введите email")
            password.isEmpty() -> showToast("Введите пароль")
            else -> authenticateUser(email, password)
        }
    }

    private fun authenticateUser(email: String, password: String) {
        lifecycleScope.launch {
            try {
                // Получаем пользователя из БД в фоновом потоке
                val user = withContext(Dispatchers.IO) {
                    userDao.getUserByEmail(email)
                }

                when {
                    user == null -> showError("Пользователь не найден")
                    !BCrypt.checkpw(password, user.password) -> showError("Неверный пароль")
                    else -> {
                        // Обновляем данные и переходим дальше
                        updateUserLogin(user)
                        navigateToMainScreen()
                    }
                }
            } catch (e: Exception) {
                Log.e("Authorization", "Login error", e)
                showError("Ошибка входа. Попробуйте позже")
            }
        }
    }

    private suspend fun updateUserLogin(user: User) {
        withContext(Dispatchers.IO) {
            try {
                // Обновляем время последнего входа
                userDao.updateLastLogin(user.id, System.currentTimeMillis())

                // Сохраняем ID пользователя
                getSharedPreferences("user_prefs", MODE_PRIVATE)
                    .edit()
                    .putInt("user_id", user.id)
                    .apply()
            } catch (e: Exception) {
                Log.e("Authorization", "Failed to update user", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@Authorization,
                        "Ошибка сохранения данных",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this@Authorization, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToRegister() {
        startActivity(Intent(this, Registration::class.java))
    }

    private fun navigateToMainScreen() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}