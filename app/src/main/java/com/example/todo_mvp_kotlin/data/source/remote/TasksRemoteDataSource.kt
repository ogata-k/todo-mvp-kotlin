package com.example.todo_mvp_kotlin.data.source.remote

import android.os.Handler
import com.example.todo_mvp_kotlin.domain.model.Task
import com.example.todo_mvp_kotlin.data.source.domain.TasksDataSource
import com.example.todo_mvp_kotlin.domain.repository.TasksRepository
import com.google.common.collect.Lists
import com.google.common.collect.Maps

object TasksRemoteDataSource : TasksDataSource {
    private const val SERVICE_LATENCY_IN_MILLIS = 5000L

    private var TASKS_SERVICE_DATA = LinkedHashMap<String, Task>()

    init {
        addTask("Build tower in Pisa", "Ground looks good, no foundation work required.")
        addTask("Finish bridge in Tacoma", "Found awesome girders at half the cost!")
    }

    private fun addTask(title: String, description: String) {
        val newTask = Task(title, description)
        TASKS_SERVICE_DATA.put(newTask.id, newTask)
    }

    override fun getTasks(callback: TasksRepository.LoadTasksCallback) {
        val tasks: List<Task> = Lists.newArrayList(TASKS_SERVICE_DATA.values)
        // 要求している実行スレッドは外部から渡されているので、ここでは特に指定しない
        // @todo remove deprecated
        Handler().run {
            postDelayed({
                callback.onTasksLoaded(tasks)
            }, SERVICE_LATENCY_IN_MILLIS)
        }
    }

    override fun getTask(taskId: String, callback: TasksRepository.GetTaskCallback) {
        val task = TASKS_SERVICE_DATA[taskId]

        // withを使ってもいいが確実に一回だけ実行させるためにrunで処理をまとめる
        // 要求している実行スレッドは外部から渡されているので、ここでは特に指定しない
        // @todo remove deprecated
        Handler().run {
            if (task != null) {
                postDelayed({ callback.onTaskLoaded(task) }, SERVICE_LATENCY_IN_MILLIS)
            } else {
                postDelayed({ callback.onDataNotAvailable() }, SERVICE_LATENCY_IN_MILLIS)
            }
        }
    }

    override fun saveTask(task: Task) {
        TASKS_SERVICE_DATA.put(task.id, task)
    }

    override fun completeTask(task: Task) {
        val completedTask = task.copy(isCompleted = true)
        TASKS_SERVICE_DATA.put(task.id, completedTask)
    }

    override fun completeTask(taskId: String) {
        val task: Task? = TASKS_SERVICE_DATA.get(taskId)
        if (task == null) {
            // usually do request remote action
            return
        }
        val completedTask = task.copy(isCompleted = true)
        TASKS_SERVICE_DATA.put(task.id, completedTask)
    }

    override fun activateTask(task: Task) {
        val activeTask = task.copy(isCompleted = false)
        TASKS_SERVICE_DATA.put(task.id, activeTask)
    }

    override fun activateTask(taskId: String) {
        val task: Task? = TASKS_SERVICE_DATA.get(taskId)
        if (task == null) {
            // usually do request remote action
            return
        }
        val activatedTask = task.copy(isCompleted = false)
        TASKS_SERVICE_DATA.put(task.id, activatedTask)
    }

    override fun clearCompletedTasks() {
        TASKS_SERVICE_DATA = Maps.newLinkedHashMap(
            TASKS_SERVICE_DATA.filterValues {
            !it.isCompleted
        })
    }

    override fun refreshTasks() {
        // APIで取得するような新しいデータはないのでリフレッシュしても更新する内容はない
    }

    override fun deleteAllTasks() {
        TASKS_SERVICE_DATA.clear()
    }

    override fun deleteTask(taskId: String) {
        TASKS_SERVICE_DATA.remove(taskId)
    }
}