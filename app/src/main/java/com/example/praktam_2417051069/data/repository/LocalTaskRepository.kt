package com.example.praktam_2417051069.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.praktam_2417051069.auth.AuthManager
import com.example.praktam_2417051069.data.model.LocalTask
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LocalTaskRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("local_tasks", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Key unik per user berdasarkan email — tidak akan bocor ke akun lain
    private val authManager = AuthManager(context)
    private val taskKey get() = "tasks_${authManager.getCurrentUserEmail()}"

    fun getAllTasks(): List<LocalTask> {
        val json = prefs.getString(taskKey, null) ?: return emptyList()
        val type = object : TypeToken<List<LocalTask>>() {}.type
        return gson.fromJson(json, type)
    }

    fun addTask(task: LocalTask) {
        val tasks = getAllTasks().toMutableList()
        tasks.add(task)
        saveTasks(tasks)
    }

    fun deleteTask(id: String) {
        val tasks = getAllTasks().toMutableList()
        tasks.removeAll { it.id == id }
        saveTasks(tasks)
    }

    fun toggleDone(id: String) {
        val tasks = getAllTasks().toMutableList()
        val index = tasks.indexOfFirst { it.id == id }
        if (index != -1) {
            tasks[index] = tasks[index].copy(isDone = !tasks[index].isDone)
        }
        saveTasks(tasks)
    }

    private fun saveTasks(tasks: List<LocalTask>) {
        prefs.edit().putString(taskKey, gson.toJson(tasks)).apply()
    }
}