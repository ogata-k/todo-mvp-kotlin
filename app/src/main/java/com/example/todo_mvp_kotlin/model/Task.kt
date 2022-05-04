package com.example.todo_mvp_kotlin.model

import java.util.*

data class Task(val title: String ="", val description: String = "", val id: String = UUID.randomUUID().toString(), val isCompleted: Boolean = false){
    val titleForList: String
        get() = if (title.isNotEmpty()) title else description

    val isActive: Boolean
        get() = !isCompleted

    val isEmpty: Boolean
        get() = title.isEmpty() && description.isEmpty()
}
