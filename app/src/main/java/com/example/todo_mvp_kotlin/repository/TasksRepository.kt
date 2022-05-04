package com.example.todo_mvp_kotlin.repository

import com.example.todo_mvp_kotlin.model.Task
import com.example.todo_mvp_kotlin.repository.abstract_source.TasksDataSource
import com.google.common.collect.Lists
import com.google.common.collect.Maps

class TasksRepository (
    val tasksRemoteDataSource: TasksDataSource,
    val tasksLocalDataSource: TasksDataSource
) : TasksDataSource {
    /**
     * This variable has public visibility so it can be accessed from tests.
     */
    var cachedTasks: LinkedHashMap<String, Task> = LinkedHashMap()

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    var cacheIsDirty = false

    /**
     * Gets tasks from cache, local data source (SQLite) or remote data source, whichever is
     * available first.
     *
     *
     * Note: [TasksDataSource.LoadTasksCallback.onDataNotAvailable] is fired if all data sources fail to
     * get the data.
     */
    override fun getTasks(callback: TasksDataSource.LoadTasksCallback) {
        // Respond immediately with cache if available and not dirty
        if (cachedTasks.isNotEmpty() && !cacheIsDirty) {
            callback.onTasksLoaded(ArrayList(cachedTasks.values))
            return
        }

        if (cacheIsDirty) {
            // If the cache is dirty we need to fetch new data from the network.
            getTasksFromRemoteDataSource(callback)
        } else {
            // Query the local storage if available. If not, query the network.
            tasksLocalDataSource.getTasks(object : TasksDataSource.LoadTasksCallback {
                override fun onTasksLoaded(tasks: List<Task>) {
                    refreshCache(tasks)
                    callback.onTasksLoaded(ArrayList(cachedTasks.values))
                }

                override fun onDataNotAvailable() {
                    getTasksFromRemoteDataSource(callback)
                }
            })
        }
    }

    override fun saveTask(task: Task) {
        // Do in memory cache update to keep the app UI up to date
        cacheAndPerform(task) {
            tasksRemoteDataSource.saveTask(it)
            tasksLocalDataSource.saveTask(it)
        }
    }

    override fun completeTask(task: Task) {
        // Do in memory cache update to keep the app UI up to date
        val completedTask = task.copy(isCompleted = true)
        cacheAndPerform(completedTask) {
            tasksRemoteDataSource.completeTask(it)
            tasksLocalDataSource.completeTask(it)
        }
    }

    override fun completeTask(taskId: String) {
        getTaskWithId(taskId)?.let {
            completeTask(it)
        }
    }

    override fun activateTask(task: Task) {
        // Do in memory cache update to keep the app UI up to date
        val completedTask = task.copy(isCompleted = true)
        cacheAndPerform(completedTask) {
            tasksRemoteDataSource.activateTask(it)
            tasksLocalDataSource.activateTask(it)
        }
    }

    override fun activateTask(taskId: String) {
        getTaskWithId(taskId)?.let {
            activateTask(it)
        }
    }

    override fun clearCompletedTasks() {
        tasksRemoteDataSource.clearCompletedTasks()
        tasksLocalDataSource.clearCompletedTasks()

        cachedTasks = Maps.newLinkedHashMap(cachedTasks.filterValues {
            !it.isCompleted
        })
    }

    /**
     * Gets tasks from local data source (sqlite) unless the table is new or empty. In that case it
     * uses the network data source. This is done to simplify the sample.
     *
     *
     * Note: [TasksDataSource.GetTaskCallback.onDataNotAvailable] is fired if both data sources fail to
     * get the data.
     */
    override fun getTask(taskId: String, callback: TasksDataSource.GetTaskCallback) {
        val taskInCache = getTaskWithId(taskId)

        // Respond immediately with cache if available
        if (taskInCache != null) {
            callback.onTaskLoaded(taskInCache)
            return
        }

        // Load from server/persisted if needed.

        // Is the task in the local data source? If not, query the network.
        tasksLocalDataSource.getTask(taskId, object : TasksDataSource.GetTaskCallback {
            override fun onTaskLoaded(task: Task) {
                // Do in memory cache update to keep the app UI up to date
                cacheAndPerform(task) {
                    callback.onTaskLoaded(it)
                }
            }

            override fun onDataNotAvailable() {
                tasksRemoteDataSource.getTask(taskId, object : TasksDataSource.GetTaskCallback {
                    override fun onTaskLoaded(task: Task) {
                        // Do in memory cache update to keep the app UI up to date
                        cacheAndPerform(task) {
                            callback.onTaskLoaded(it)
                        }
                    }

                    override fun onDataNotAvailable() {
                        callback.onDataNotAvailable()
                    }
                })
            }
        })
    }

    override fun refreshTasks() {
        cacheIsDirty = true
    }

    override fun deleteAllTasks() {
        tasksRemoteDataSource.deleteAllTasks()
        tasksLocalDataSource.deleteAllTasks()
        cachedTasks.clear()
    }

    override fun deleteTask(taskId: String) {
        tasksRemoteDataSource.deleteTask(taskId)
        tasksLocalDataSource.deleteTask(taskId)
        cachedTasks.remove(taskId)
    }

    private fun getTasksFromRemoteDataSource(callback: TasksDataSource.LoadTasksCallback) {
        tasksRemoteDataSource.getTasks(object : TasksDataSource.LoadTasksCallback {
            override fun onTasksLoaded(tasks: List<Task>) {
                refreshCache(tasks)
                refreshLocalDataSource(tasks)
                callback.onTasksLoaded(Lists.newArrayList(cachedTasks.values))
            }

            override fun onDataNotAvailable() {
                callback.onDataNotAvailable()
            }
        })
    }

    private fun refreshCache(tasks: List<Task>) {
        cachedTasks.clear()
        tasks.forEach {
            cacheAndPerform(it) {}
        }
        cacheIsDirty = false
    }

    private fun refreshLocalDataSource(tasks: List<Task>) {
        tasksLocalDataSource.deleteAllTasks()
        for (task in tasks) {
            tasksLocalDataSource.saveTask(task)
        }
    }

    private fun getTaskWithId(id: String): Task? = cachedTasks[id]

    private inline fun cacheAndPerform(task: Task, perform: (Task) -> Unit) {
        cachedTasks.put(task.id, task)
        perform(task)
    }

    companion object {

        private var INSTANCE: TasksRepository? = null

        /**
         * Returns the single instance of this class, creating it if necessary.
         * @param tasksRemoteDataSource the backend data source
         * *
         * @param tasksLocalDataSource  the device storage data source
         * *
         * @return the [TasksRepository] instance
         */
        @JvmStatic fun getInstance(tasksRemoteDataSource: TasksDataSource,
                                   tasksLocalDataSource: TasksDataSource): TasksRepository {
            return INSTANCE ?: TasksRepository(tasksRemoteDataSource, tasksLocalDataSource)
                .apply { INSTANCE = this }
        }

        /**
         * Used to force [getInstance] to create a new instance
         * next time it's called.
         */
        @JvmStatic fun destroyInstance() {
            INSTANCE = null
        }
    }
}