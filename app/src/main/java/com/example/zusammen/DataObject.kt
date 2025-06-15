package com.example.zusammen

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.Date

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "password") var password: String,
    @ColumnInfo(name = "last_login") val lastLogin: Long? = null,
    @ColumnInfo(name = "login_attempts") val loginAttempts: Int = 0,
    @ColumnInfo(name = "username") val username: String? = null,
    @ColumnInfo(name = "birth_date") val birthDate: String? = null,
    @ColumnInfo(name = "city") val city: String? = null,
    @ColumnInfo(name = "avatarUri") var avatarUri: String?  = null
)

@Entity(
    tableName = "notes",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["user_id"])]
)
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "user_id") val userId: Int
)


@Entity(
    tableName = "events",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["user_id"])]
)
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "time") val time: String,
    @ColumnInfo(name = "date") val date: Long,
    @ColumnInfo(name = "user_id") val userId: Int
) {
    fun getCalendar(): Calendar {
        return Calendar.getInstance().apply {
            timeInMillis = date
        }
    }
}

