package com.example.praktam_2417051069.data.model

data class LocalTask(
    val id: String,
    val judul: String,
    val deskripsi: String,
    val deadline: String,
    val isDone: Boolean = false,
    val kategori: String = "Tugas"
)
