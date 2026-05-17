package com.example.praktam_2417051069.data.api

import com.example.praktam_2417051069.data.model.StudentPlanner
import retrofit2.http.GET

interface ApiService {

    @GET("studentplanner.json")
    suspend fun getStudents(): List<StudentPlanner>
}