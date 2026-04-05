package com.arirang.beautylounge

import java.io.Serializable

data class Service(
    val id: String,
    val name: String,
    val category: String,
    val price: Int,
    val durationMin: Int,
    val durationMax: Int
) : Serializable
