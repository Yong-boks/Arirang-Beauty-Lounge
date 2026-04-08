package com.arirang.beautylounge

data class CustomerSummary(
    val customerId: String = "",
    val customerName: String = "",
    val bookingCount: Int = 0,
    val lastService: String = "",
    val lastDate: String = ""
)
