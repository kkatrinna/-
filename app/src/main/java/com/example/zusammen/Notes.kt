package com.example.zusammen

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zusammen.databinding.ActivityNotesBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Notes : AppCompatActivity() {
    private lateinit var binding: ActivityNotesBinding
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var noteDao: NoteDao
    private var userId: Int = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация базы данных
        noteDao = AppDatabase.getDatabase(this).noteDao()
        userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getInt("user_id", -1)

        notesAdapter = NotesAdapter(
            onNoteClick = { note ->
            },
            onNoteLongClick = { note ->
                showDeleteDialog(note)
                true
            },
            onEditClick = { note ->
                openEditNote(note)
            },
            onDeleteClick = { note ->
                showDeleteDialog(note)
            }
        )

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

        setupSearchView()
        setupToolbar()
        setupRecyclerView()
        setupFab()
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { filterNotes(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { filterNotes(it) }
                return true
            }
        })
    }

    private fun filterNotes(query: String) {
        lifecycleScope.launch {
            val allNotes = withContext(Dispatchers.IO) {
                noteDao.getNotesByUser(userId)
            }

            val filteredNotes = if (query.isEmpty()) {
                allNotes
            } else {
                allNotes.filter { note ->
                    note.title.contains(query, true) ||
                            note.content.contains(query, true)
                }
            }

            notesAdapter.submitList(filteredNotes)
        }
    }


    private fun openEditNote(note: Note) {
        Intent(this, AddNote::class.java).apply {
            putExtra("note_id", note.id)
            putExtra("note_title", note.title)
            putExtra("note_content", note.content)
            startActivity(this)
        }
    }

    override fun onResume() {
        super.onResume()
        loadNotes()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        with(binding.notesRecyclerView) {
            layoutManager = LinearLayoutManager(this@Notes)
            adapter = notesAdapter
            addItemDecoration(
                DividerItemDecoration(
                    this@Notes,
                    LinearLayoutManager.VERTICAL
                )
            )
        }
    }

    private fun setupFab() {
        binding.fabAddNote.setOnClickListener {
            startActivity(Intent(this, AddNote::class.java))
        }
    }

    private fun loadNotes() {
        lifecycleScope.launch {
            val notes = withContext(Dispatchers.IO) {
                noteDao.getNotesByUser(userId)
            }
            notesAdapter.submitList(notes)
        }
    }

    private fun showDeleteDialog(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Удалить заметку")
            .setMessage("Вы уверены, что хотите удалить эту заметку?")
            .setPositiveButton("Удалить") { _, _ ->
                deleteNote(note)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteNote(note: Note) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                noteDao.delete(note)
            }
            loadNotes()
        }
    }
}