package com.example.meerkatservice.extensions

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun String.fromIsoToDate(): Date {
    val isoFormat = "yyyy-MM-dd'T'HH:mm:ssX"
    kotlin.runCatching {
        SimpleDateFormat(isoFormat, Locale.US).parse(this)
    }.onSuccess {
        return it ?: Date()
    }.onFailure {
        return Date(0)
    }
    return Date(0)
}

fun Date.toBestString(): String {
    kotlin.runCatching {
        val locale = Locale.getDefault()
        val pattern = DateFormat.getBestDateTimePattern(locale, "yyyyMMMdEEEHHmmss")
        SimpleDateFormat(pattern, locale).format(this@toBestString)
    }.onSuccess {
        return it
    }.onFailure {
        return it.message ?: ""
    }
    return ""
}

fun Date.toMinuteString(): String {
    kotlin.runCatching {
        val locale = Locale.getDefault()
        val pattern = DateFormat.getBestDateTimePattern(locale, "mm")
        SimpleDateFormat(pattern, locale).format(this@toMinuteString)
    }.onSuccess {
        return it
    }.onFailure {
        return it.message ?: ""
    }
    return ""
}
