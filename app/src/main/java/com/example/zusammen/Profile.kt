package com.example.zusammen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.zusammen.databinding.ActivityProfileBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Profile : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var userDao: UserDao

    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
                startActivity(Intent(this, MainActivity::class.java))
                overridePendingTransition(0, 0)
                true
            }
            R.id.nav_chat -> {
                startActivity(Intent(this, Chat::class.java))
                overridePendingTransition(0, 0)
                true
            }
            R.id.nav_calendar -> {
                startActivity(Intent(this, CalendActivity::class.java))
                overridePendingTransition(0, 0)
                true
            }
            R.id.nav_profile -> {
                true
            }
            else -> false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


        userDao = AppDatabase.getDatabase(applicationContext).userDao()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_profile // Выделяем текущий пункт (Главная)

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
                    startActivity(Intent(this, Chat::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_profile -> {
                    true
                }
                else -> false
            }
        }

        setupClickListeners()
        loadUserData()

        binding.btnMyNotes.setOnClickListener {
            startActivity(Intent(this, Notes::class.java))
        }
    }

    private fun setupClickListeners() {
        // Кнопка редактирования профиля
        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, ProfileEdit::class.java))
        }

        // Обработчики для BottomNavigationView
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            try {
                val userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    .getInt("user_id", -1)

                if (userId == -1) {
                    showError("Пользователь не авторизован")
                    navigateToLogin()
                    return@launch
                }

                val user = withContext(Dispatchers.IO) {
                    userDao.getUserById(userId)
                } ?: run {
                    showError("Пользователь не найден")
                    return@launch
                }

                // Установка текстовых данных
                with(binding) {
                    tvBirthday.text = user.birthDate ?: "Не указана"
                    textViewFullName.text = user.username ?: "Ваше имя"
                    tvCity.text = user.city ?: "Не указан"
                    tvEmail.text = user.email
                }

                // Загрузка аватарки
                loadUserAvatar(user.avatarUri)

            } catch (e: Exception) {
                showError("Ошибка загрузки данных")
                Log.e("Profile", "Error loading user data", e)
            }
        }
    }

    private fun loadUserAvatar(avatarUri: String?) {
        if (avatarUri.isNullOrEmpty()) {
            binding.imgAvatar.setImageResource(R.drawable.default_avatar)
            return
        }

        try {
            Glide.with(this)
                .load(Uri.parse(avatarUri))
                .circleCrop()
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(binding.imgAvatar)
        } catch (e: Exception) {
            Log.e("Profile", "Error loading avatar", e)
            binding.imgAvatar.setImageResource(R.drawable.default_avatar)
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, Authorization::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

}

