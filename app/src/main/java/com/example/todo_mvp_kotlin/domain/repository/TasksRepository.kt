package com.example.todo_mvp_kotlin.domain.repository

import com.example.todo_mvp_kotlin.domain.model.Task

interface TasksRepository {
    interface LoadTasksCallback {
        fun onTasksLoaded(tasks: List<Task>)
        /// 初回読み取り時にデータが存在しない場合のコールバック
        fun onDataNotAvailable()
    }

    interface GetTaskCallback {
        fun onTaskLoaded(task: Task)
        fun onDataNotAvailable()
    }

    fun getTasks(callback: LoadTasksCallback)
    fun getTask(taskId: String, callback: GetTaskCallback)
    fun saveTask(task: Task)
    fun completeTask(task: Task)
    fun completeTask(taskId: String)
    fun activateTask(task: Task)
    fun activateTask(taskId: String)
    fun clearCompletedTasks()
    fun refreshTasks()
    fun deleteAllTasks()
    fun deleteTask(taskId: String)
}