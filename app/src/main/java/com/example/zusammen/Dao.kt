package com.example.zusammen

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


import androidx.room.*

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
}