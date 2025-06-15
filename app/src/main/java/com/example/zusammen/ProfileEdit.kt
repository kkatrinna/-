package com.example.zusammen

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.zusammen.databinding.ActivityProfileEditBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt
import java.text.SimpleDateFormat
import java.util.*

class ProfileEdit : AppCompatActivity() {

    private lateinit var binding: ActivityProfileEditBinding
    private lateinit var userDao: UserDao
    private var currentUser: User? = null
    private val calendar = Calendar.getInstance()
    private val russianLocale = Locale("ru", "RU")
    private var imageUri: Uri? = null

    // Контракты для запроса разрешений
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            showImageSourceDialog()
        } else {
            Toast.makeText(this, "Разрешения не получены", Toast.LENGTH_SHORT).show()
        }
    }

    // Контракт для выбора изображения из галереи
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                imageUri = uri
                binding.imgAvatar.setImageURI(uri)
            }
        }
    }

    // Контракт для съемки фото
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri?.let {
                binding.imgAvatar.setImageURI(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация базы данных
        userDao = AppDatabase.getDatabase(applicationContext).userDao()

        setupUI()
        loadUserData()
    }

    private fun setupUI() {
        setupToolbar()
        setupInputFields()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.title = "Редактирование профиля"
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupInputFields() {
        with(binding) {
            etUserName.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS
            tvSelectedCity.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS
            etEmail.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            editDate.setOnClickListener { showDatePicker() }
            imgAvatar.setOnClickListener { checkPermissionsAndSelectImage() }
            btnSaveChanges.setOnClickListener { validateAndSaveChanges() }
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            try {
                val userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    .getInt("user_id", -1)

                if (userId != -1) {
                    currentUser = withContext(Dispatchers.IO) {
                        userDao.getUserById(userId)
                    }

                    currentUser?.let { user ->
                        with(binding) {
                            etUserName.setText(user.username ?: "")
                            tvSelectedCity.setText(user.city ?: "")
                            etEmail.setText(user.email)
                            editDate.setText(user.birthDate ?: "")
                            // Здесь можно загрузить аватарку, если она есть
                        }
                    }
                }
            } catch (e: Exception) {
                showToast("Ошибка загрузки данных")
            }
        }
    }

    private fun checkPermissionsAndSelectImage() {
        val requiredPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (requiredPermissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            showImageSourceDialog()
        } else {
            requestPermissionLauncher.launch(requiredPermissions)
        }
    }

    private fun showImageSourceDialog() {
        AlertDialog.Builder(this)
            .setTitle("Выберите источник")
            .setItems(arrayOf("Камера", "Галерея")) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                updateDateInView()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    private fun updateDateInView() {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", russianLocale)
        binding.editDate.setText(dateFormat.format(calendar.time))
    }

    private fun validateAndSaveChanges() {
        with(binding) {
            val username = etUserName.text.toString().trim()
            val city = tvSelectedCity.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val birthDate = editDate.text.toString().trim()
            val currentPassword = etCurrentPassword.text.toString()
            val newPassword = etNewPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()
            val avatarUri = imageUri?.toString()

            if (username.isEmpty()) {
                showToast("Введите имя пользователя")
                return
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showToast("Введите корректный email")
                return
            }

            lifecycleScope.launch {
                try {
                    currentUser?.let { user ->
                        val updatedPassword = handlePasswordChange(
                            user,
                            currentPassword,
                            newPassword,
                            confirmPassword
                        ) ?: return@launch

                        val updatedUser = user.copy(
                            username = username,
                            city = city.ifEmpty { null },
                            email = email,
                            birthDate = birthDate.ifEmpty { null },
                            password = updatedPassword,
                            avatarUri = imageUri?.toString()
                        )

                        withContext(Dispatchers.IO) {
                            userDao.updateUser(updatedUser)
                        }

                        setResult(RESULT_OK)
                        showToast("Данные сохранены")
                        finish()
                    } ?: showToast("Пользователь не найден")
                } catch (e: Exception) {
                    showToast("Ошибка сохранения: ${e.localizedMessage}")
                }
            }
        }
    }

    private suspend fun handlePasswordChange(
        user: User,
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): String? {
        return when {
            currentPassword.isNotEmpty() || newPassword.isNotEmpty() || confirmPassword.isNotEmpty() -> {
                if (!validatePasswordChange(user, currentPassword, newPassword, confirmPassword)) {
                    return null
                }
                BCrypt.hashpw(newPassword, BCrypt.gensalt())
            }
            else -> user.password
        }
    }

    private fun validatePasswordChange(
        user: User,
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Boolean {
        if (currentPassword.isEmpty()) {
            showToast("Введите текущий пароль")
            return false
        }

        if (!BCrypt.checkpw(currentPassword, user.password)) {
            showToast("Неверный текущий пароль")
            return false
        }

        if (newPassword.isEmpty()) {
            showToast("Введите новый пароль")
            return false
        }

        if (newPassword.length < 8) {
            showToast("Пароль должен содержать минимум 8 символов")
            return false
        }

        if (newPassword != confirmPassword) {
            showToast("Пароли не совпадают")
            return false
        }

        return true
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}