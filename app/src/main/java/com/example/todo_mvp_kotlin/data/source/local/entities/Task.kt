package com.example.todo_mvp_kotlin.data.source.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*
import com.example.todo_mvp_kotlin.domain.model.Task as modelTask;

/**
 * Model class for a Task.
 *
 * @param title       title of the task
 * @param description description of the task
 * @param id          id of the task
 * @param isCompleted the task is completed?
 */
@Entity(tableName = "tasks")
data class Task @JvmOverloads constructor(
    @ColumnInfo(name = "title") var title: String = "",
    @ColumnInfo(name = "description") var description: String = "",
    @PrimaryKey @ColumnInfo(name = "entryid") var id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "completed") var isCompleted: Boolean = false,
) {
    companion object {
        fun fromModel(task: modelTask): Task {
            return Task(task.title, task.description, task.id, task.isCompleted)
        }
    }

    fun toModel(): modelTask
    {
        return modelTask(title, description, id, isCompleted)
    }
}
