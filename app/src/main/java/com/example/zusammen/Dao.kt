package com.example.zusammen

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.util.Date


@Dao
interface UserDao {
    // Проверяет наличие указанного email
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    fun isEmailExists(email: String): Boolean

    // Добавляет пользователя
    @Insert
    fun insertUser(user: User)

    // Получает пользователя по email
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun getUserByEmail(email: String): User?

    // Обновляет данные пользователя
    @Update
    fun updateUser(user: User)

    // Обновляет последний вход пользователя
    @Query("UPDATE users SET last_login = :timestamp WHERE id = :userId")
    fun updateLastLogin(userId: Int, timestamp: Long)

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: Int): User?
}

@Dao
interface NoteDao {
    @Insert
    fun insert(note: Note)

    @Update
    fun update(note: Note)

    @Delete
    fun delete(note: Note)

    @Query("SELECT * FROM notes WHERE user_id = :userId ORDER BY created_at DESC")
    fun getNotesByUser(userId: Int): List<Note>

    @Query("SELECT * FROM notes WHERE user_id = :userId AND (title LIKE :query OR content LIKE :query) ORDER BY created_at DESC")
    fun searchNotes(userId: Int, query: String): List<Note>

    @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
    fun getNoteById(noteId: Int): Note?
}


@Dao
interface EventDao {
    @Insert
    fun insert(event: Event): Long

    @Update
    fun update(event: Event)

    @Query("SELECT * FROM events WHERE user_id = :userId AND date BETWEEN :start AND :end ORDER BY date ASC")
    fun getEventsByDateRange(userId: Int, start: Long, end: Long): List<Event>

    @Query("DELETE FROM events WHERE id = :eventId")
    fun delete(eventId: Int)
}