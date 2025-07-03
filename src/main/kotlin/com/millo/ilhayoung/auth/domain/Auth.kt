package com.millo.ilhayoung.auth.domain

import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "auth")
data class Auth(
    val id: String,
    val provider: String,
    val providerId: String,
    val email: String,
    val name: String
)
