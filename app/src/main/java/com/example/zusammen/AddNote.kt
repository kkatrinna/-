package com.example.zusammen

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.zusammen.databinding.ActivityAddNoteBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddNote : AppCompatActivity() {
    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var noteDao: NoteDao
    private var noteId: Int = -1
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        noteDao = AppDatabase.getDatabase(this).noteDao()

        // Проверяем, открываем ли заметку для редактирования
        noteId = intent.getIntExtra("note_id", -1)
        if (noteId != -1) {
            isEditMode = true
            val title = intent.getStringExtra("note_title") ?: ""
            val content = intent.getStringExtra("note_content") ?: ""

            binding.etNoteTitle.setText(title)
            binding.etNoteContent.setText(content)
            binding.toolbar.title = "Редактировать заметку"
        }

        setupToolbar()
        setupSaveButton()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupSaveButton() {
        binding.btnSaveNote.setOnClickListener {
            saveNote()
        }
    }

    private fun saveNote() {
        val title = binding.etNoteTitle.text.toString().trim()
        val content = binding.etNoteContent.text.toString().trim()

        if (title.isEmpty()) {
            binding.etNoteTitle.error = "Введите заголовок"
            return
        }

        if (content.isEmpty()) {
            binding.etNoteContent.error = "Введите текст заметки"
            return
        }

        lifecycleScope.launch {
            try {
                if (isEditMode) {
                    // Обновляем существующую заметку
                    withContext(Dispatchers.IO) {
                        noteDao.update(Note(
                            id = noteId,
                            title = title,
                            content = content,
                            userId = getCurrentUserId(),
                            createdAt = System.currentTimeMillis() // Обновляем время изменения
                        ))
                    }
                    Toast.makeText(this@AddNote, "Заметка обновлена", Toast.LENGTH_SHORT).show()
                } else {
                    // Создаем новую заметку
                    withContext(Dispatchers.IO) {
                        noteDao.insert(Note(
                            title = title,
                            content = content,
                            userId = getCurrentUserId(),
                            createdAt = System.currentTimeMillis()
                        ))
                    }
                    Toast.makeText(this@AddNote, "Заметка сохранена", Toast.LENGTH_SHORT).show()
                }
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AddNote, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentUserId(): Int {
        return getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getInt("user_id", -1)
    }

    override fun onBackPressed() {
        if (hasChanges()) {
            showUnsavedChangesDialog()
        } else {
            super.onBackPressed()
        }
    }

    private fun hasChanges(): Boolean {
        val originalTitle = intent.getStringExtra("note_title") ?: ""
        val originalContent = intent.getStringExtra("note_content") ?: ""

        return binding.etNoteTitle.text.toString() != originalTitle ||
                binding.etNoteContent.text.toString() != originalContent
    }

    private fun showUnsavedChangesDialog() {
        AlertDialog.Builder(this)
            .setTitle("Несохраненные изменения")
            .setMessage("Сохранить изменения перед выходом?")
            .setPositiveButton("Сохранить") { _, _ ->
                saveNote()
            }
            .setNegativeButton("Не сохранять") { _, _ ->
                finish()
            }
            .setNeutralButton("Отмена", null)
            .show()
    }
}