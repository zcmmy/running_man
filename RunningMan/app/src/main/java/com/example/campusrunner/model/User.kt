package com.example.campusrunner.model

import java.util.Date

data class UserProfile(
    val id: String,
    val studentId: String,
    val name: String,
    val avatar: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val creditScore: Double = 5.0,
    val totalOrders: Int = 0,
    val banlance: Double = 0.0,
    val createdAt: Date
)

data class LoginRequest(
    val studentId: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: UserProfile
)