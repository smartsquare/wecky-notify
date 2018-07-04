package de.smartsquare.wecky

import com.amazonaws.services.dynamodbv2.model.StreamRecord
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.*

class JacksonAwsMapperBuilder {
    fun build() = jacksonObjectMapper()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .addMixIn(StreamRecord::class.java, StreamRecordMixin::class.java)

    //little hack needed, since this field is coming along as a float (?!)
    class StreamRecordMixin(
            @JsonIgnore
            val approximateCreationDateTime: Date
    )
}