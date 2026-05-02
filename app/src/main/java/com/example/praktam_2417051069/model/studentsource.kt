package com.example.praktam_2417051069.model

import android.content.Context

object StudentSource {

    fun getResourceId(context: Context, imageName: String): Int {
        return context.resources.getIdentifier(
            imageName,
            "drawable",
            context.packageName
        )
    }
}