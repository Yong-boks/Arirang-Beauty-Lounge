package com.arirang.beautylounge

import java.io.Serializable

data class StaffMember(
    val staffId: String,
    val name: String,
    val employeeId: String,
    val services: List<String>,
    val specialization: String
) : Serializable
