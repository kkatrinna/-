package com.example.zusammen

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.zusammen.databinding.ActivityRegistrationBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt

class Registration : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация базы данных
        userDao = AppDatabase.getDatabase(applicationContext).userDao()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.apply {
            btnSSigned.setOnClickListener {
                handleRegistration()
            }

            tvRedirectLogin.setOnClickListener {
                navigateToLogin()
            }
        }
    }

    private fun handleRegistration() {
        val email = binding.etSEmail.text.toString().trim()
        val password = binding.etSPassword.text.toString().trim()
        val confirmedPassword = binding.etSConfPassword.text.toString().trim()

        if (validateRegistrationForm(email, password, confirmedPassword)) {
            registerNewUser(email, password)
        }
    }

    private fun validateRegistrationForm(
        email: String,
        password: String,
        confirmedPassword: String
    ): Boolean {
        return when {
            email.isEmpty() -> {
                showToast("Введите адрес электронной почты")
                false
            }
            !isValidEmail(email) -> {
                showToast("Некорректный адрес электронной почты")
                false
            }
            password.isEmpty() -> {
                showToast("Введите пароль")
                false
            }
            !isValidPassword(password) -> {
                showPasswordRequirements()
                false
            }
            password != confirmedPassword -> {
                showToast("Пароли не совпадают")
                false
            }
            else -> true
        }
    }

    private fun registerNewUser(email: String, password: String) {
        lifecycleScope.launch {
            try {
                // Проверяем email в фоновом потоке
                val emailExists = withContext(Dispatchers.IO) {
                    userDao.getUserByEmail(email) != null
                }

                if (emailExists) {
                    showToast("Пользователь с таким email уже существует")
                    return@launch
                }

                // Хешируем пароль и создаем пользователя
                val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
                val user = User(
                    email = email,
                    password = hashedPassword,
                    lastLogin = null
                )

                // Вставляем пользователя в БД
                withContext(Dispatchers.IO) {
                    userDao.insertUser(user)
                }

                showRegistrationSuccess()
                navigateToLogin()
                finish()

            } catch (e: Exception) {
                showToast("Ошибка регистрации: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,}\$"
        return email.matches(emailRegex.toRegex()) && email.length <= 100
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { it.isDigit() } &&
                password.any { !it.isLetterOrDigit() }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPasswordRequirements() {
        runOnUiThread {
            Snackbar.make(
                binding.root,
                "Пароль должен содержать: 8+ символов, заглавные и строчные буквы, цифры, спецсимволы",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun showRegistrationSuccess() {
        runOnUiThread {
            Toast.makeText(
                this,
                "Регистрация прошла успешно!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, Authorization::class.java))
    }
}