package de.smartsquare.wecky.domain

data class WebsiteChange(
        val id: String,
        val url: String,
        val content: String,
        val userEmail: String
)