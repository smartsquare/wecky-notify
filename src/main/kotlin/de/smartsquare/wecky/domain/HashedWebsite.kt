package de.smartsquare.wecky.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
data class HashedWebsite(
        val websiteId: String,
        val url: String,
        val content: String,
        val hash: Int = content.hashCode(),
        val crawlDate: Instant = Instant.now()
)