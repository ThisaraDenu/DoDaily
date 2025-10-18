package com.example.dodaily.model

import java.io.Serializable

data class HydrationSchedule(
    val id: String,
    val time: String,
    val description: String,
    var isCompleted: Boolean
) : Serializable
