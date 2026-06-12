package com.example.praktam_2417051069.data.model

import com.google.gson.annotations.SerializedName

data class StudentPlanner(
    @SerializedName("judul")
    val nama: String,

    @SerializedName("kategori")
    val deadline: String,

    @SerializedName("isi")
    val deskripsi: String,

    @SerializedName("image_url")
    val imageUrl: String
)