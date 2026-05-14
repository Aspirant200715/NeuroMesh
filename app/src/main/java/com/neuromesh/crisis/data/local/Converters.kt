package com.neuromesh.crisis.data.local

import androidx.room.TypeConverter
import com.neuromesh.crisis.data.model.CrisisType

class Converters {
    @TypeConverter
    fun fromCrisisType(value: CrisisType): String = value.name

    @TypeConverter
    fun toCrisisType(value: String): CrisisType = CrisisType.valueOf(value)
}
