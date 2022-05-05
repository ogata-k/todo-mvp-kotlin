package com.example.todo_mvp_kotlin.data.source.local

import androidx.annotation.VisibleForTesting
import com.example.todo_mvp_kotlin.domain.model.Task as ModelTask;
import com.example.todo_mvp_kotlin.data.source.local.dao.TasksDao
import com.example.todo_mvp_kotlin.data.source.local.entities.Task as EntityTask;
import com.example.todo_mvp_kotlin.data.source.domain.TasksDataSource
import com.example.todo_mvp_kotlin.domain.repository.TasksRepository
import com.example.todo_mvp_kotlin.util.AppExecutors

class TasksLocalDataSource  private constructor(
    val appExecutors: AppExecutors,
    val tasksDao: TasksDao
) : TasksDataSource {
    /**
     * Note: [TasksDataSource.LoadTasksCallback.onDataNotAvailable] is fired if the database doesn't exist
     * or the table is empty.
     */
    override fun getTasks(callback: TasksRepository.LoadTasksCallback) {
        appExecutors.diskIO.execute {
            val tasks: List<ModelTask> = tasksDao.getTasks().map { it.toModel() }
            appExecutors.mainThread.execute {
                if (tasks.isEmpty()) {
                    // This will be called if the table is new or just empty.
                    callback.onDataNotAvailable()
                } else {
                    callback.onTasksLoaded(tasks)
                }
            }
        }
    }

    /**
     * Note: [TasksDataSource.GetTaskCallback.onDataNotAvailable] is fired if the [Task] isn't
     * found.
     */
    override fun getTask(taskId: String, callback: TasksRepository.GetTaskCallback) {
        appExecutors.diskIO.execute {
            val task = tasksDao.getTaskById(taskId)?.toModel()
            appExecutors.mainThread.execute {
                if (task != null) {
                    callback.onTaskLoaded(task)
                } else {
                    callback.onDataNotAvailable()
                }
            }
        }
    }

    override fun saveTask(task: ModelTask) {
        appExecutors.diskIO.execute { tasksDao.insertTask(EntityTask.fromModel(task)) }
    }

    override fun completeTask(taskId: String) {
        appExecutors.diskIO.execute { tasksDao.updateCompleted(taskId, true) }
    }

    override fun activateTask(taskId: String) {
        appExecutors.diskIO.execute { tasksDao.updateCompleted(taskId, false) }
    }

    override fun clearCompletedTasks() {
        appExecutors.diskIO.execute { tasksDao.deleteCompletedTasks() }
    }

    override fun refreshTasks() {
        // Not required because the {@link TasksRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
    }

    override fun deleteAllTasks() {
        appExecutors.diskIO.execute { tasksDao.deleteTasks() }
    }

    override fun deleteTask(taskId: String) {
        appExecutors.diskIO.execute { tasksDao.deleteTaskById(taskId) }
    }

    companion object {
        private var INSTANCE: TasksLocalDataSource? = null

        @JvmStatic
        fun getInstance(appExecutors: AppExecutors, tasksDao: TasksDao): TasksLocalDataSource {
            if (INSTANCE == null) {
                synchronized(TasksLocalDataSource::javaClass) {
                    INSTANCE = TasksLocalDataSource(appExecutors, tasksDao)
                }
            }
            return INSTANCE!!
        }

        @VisibleForTesting
        fun clearInstance() {
            INSTANCE = null
        }
    }
}