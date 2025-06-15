package com.example.voltix.util

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import java.io.IOException
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.google.gson.stream.JsonWriter
import java.time.format.DateTimeParseException

class LocalTimeAdapter : TypeAdapter<LocalTime>() {
    private val formatter = DateTimeFormatter.ISO_LOCAL_TIME

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: LocalTime){
        if (value == null ){
            out.nullValue()
        } else {
            out.value(formatter.format(value))
        }
    }

    @Throws(IOException::class)
    override fun read(input: JsonReader): LocalTime? {
        return when (input.peek()) {
            JsonToken.NULL -> {
                input.nextNull()
                null
            }
            JsonToken.STRING -> {
                val timeString = input.nextString()
                try {
                    LocalTime.parse(timeString, formatter)
                } catch (e: DateTimeParseException) {
                    throw IOException("Could not parse LocalTime: $timeString", e)
                }
            }
            else -> {
                input.skipValue()
                null
            }
        }
    }
}