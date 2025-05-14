package com.example.voltix.data.database

import androidx.room.TypeConverter
import com.example.voltix.data.entity.JenisRuangan
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_TIME
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.format(dateTimeFormatter)
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, dateTimeFormatter) }
    }
    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? {
        return value?.format(formatter)
    }

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? {
        return value?.let {
            LocalTime.parse(it, formatter)
        }
    }

    @TypeConverter
    fun fromJenisRuangan(value: JenisRuangan): String {
        return value.name
    }

    @TypeConverter
    fun toJenisRuangan(value: String): JenisRuangan {
        return JenisRuangan.valueOf(value)
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? =
        value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? =
        date?.time
}
