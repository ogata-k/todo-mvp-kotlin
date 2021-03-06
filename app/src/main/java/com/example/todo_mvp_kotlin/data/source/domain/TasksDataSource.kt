package com.example.todo_mvp_kotlin.data.source.domain

import com.example.todo_mvp_kotlin.domain.model.Task
import com.example.todo_mvp_kotlin.domain.repository.TasksRepository

// @todo 必要ならリモートとローカルで分ける
interface TasksDataSource {
    fun getTasks(callback: TasksRepository.LoadTasksCallback)
    fun getTask(taskId: String, callback: TasksRepository.GetTaskCallback)
    fun saveTask(task: Task)
    fun completeTask(taskId: String)
    fun activateTask(taskId: String)
    fun clearCompletedTasks()
    fun refreshTasks()
    fun deleteAllTasks()
    fun deleteTask(taskId: String)
}