package com.example.praktam_2417051069.data.repository
import com.example.praktam_2417051069.data.api.RetrofitClient
import com.example.praktam_2417051069.data.model.StudentPlanner

class Studentrepository{
    suspend fun getStudents(): List<StudentPlanner> {
        return try{
            RetrofitClient.instance.getStudents()
        } catch (e: Exception){
            emptyList()
        }
    }
}