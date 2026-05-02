package com.example.praktam_2417051069.network

import com.example.praktam_2417051069.model.StudentPlanner
import retrofit2.http.GET

interface ApiService {

    @GET("studentplanner.json")
    suspend fun getStudents(): List<StudentPlanner>
}