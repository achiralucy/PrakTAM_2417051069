package com.example.praktam_2417051069.model

import com.google.gson.annotations.SerializedName

data class StudentPlanner(
    @SerializedName("nama")
    val nama: String,

    @SerializedName("deadline")
    val deadline: String,

    @SerializedName("deskripsi")
    val deskripsi: String,

    @SerializedName("image_Url")
    val imageUrl: String
)