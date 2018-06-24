package de.smartsquare.wecky.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class WebsiteChange(
        val id: String,
        val url: String,
        val content: String,
        val userEmail: String
)