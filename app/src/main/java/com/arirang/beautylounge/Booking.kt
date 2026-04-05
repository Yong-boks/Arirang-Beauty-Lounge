package com.arirang.beautylounge

data class Booking(
    val bookingId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val serviceCategory: String = "",
    val staffId: String = "",
    val staffName: String = "",
    val date: String = "",
    val time: String = "",
    val price: Int = 0,
    val durationMin: Int = 0,
    val durationMax: Int = 0,
    val status: String = "Confirmed",
    val createdAt: Long = 0L
)
