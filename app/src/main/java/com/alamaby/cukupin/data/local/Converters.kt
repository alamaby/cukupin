package com.alamaby.cukupin.data.local

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate

/** Konverter Room: LocalDate untuk tanggal keuangan, Instant untuk audit. */
class Converters {

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? =
        value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long?): Instant? =
        value?.let { Instant.ofEpochMilli(it) }
}
