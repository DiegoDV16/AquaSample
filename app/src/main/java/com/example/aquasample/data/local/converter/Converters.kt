package com.example.aquasample.data.local.converter

import androidx.room.TypeConverter
import com.example.aquasample.data.session.UserRole
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value ?: emptyList<String>())
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        val listType = object : TypeToken<List<String>>() {}.type
        return try {
            gson.fromJson(value, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromStringSet(value: Set<String>?): String {
        return gson.toJson(value ?: emptySet<String>())
    }

    @TypeConverter
    fun toStringSet(value: String?): Set<String> {
        if (value.isNullOrBlank()) return emptySet()
        val setType = object : TypeToken<Set<String>>() {}.type
        return try {
            gson.fromJson(value, setType) ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    @TypeConverter
    fun fromUserRole(role: UserRole?): String {
        return role?.name ?: UserRole.OPERADOR.name
    }

    @TypeConverter
    fun toUserRole(value: String?): UserRole {
        return try {
            if (!value.isNullOrBlank()) UserRole.valueOf(value) else UserRole.OPERADOR
        } catch (e: Exception) {
            UserRole.OPERADOR
        }
    }
}
