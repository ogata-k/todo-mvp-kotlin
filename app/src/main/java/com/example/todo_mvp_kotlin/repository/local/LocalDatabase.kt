package com.example.todo_mvp_kotlin.repository.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.todo_mvp_kotlin.repository.local.dao.TasksDao
import com.example.todo_mvp_kotlin.repository.local.entities.Task

@Database(entities = [Task::class], version = 1, exportSchema = true)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun taskDao(): TasksDao

    companion object {
        private var INSTANCE: LocalDatabase? = null

        private val lock = Any()

        fun getInstance(context: Context): LocalDatabase {
            synchronized(lock) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        LocalDatabase::class.java, "todo_database.db")
                        .build()
                }
                return INSTANCE!!
            }
        }
    }
}