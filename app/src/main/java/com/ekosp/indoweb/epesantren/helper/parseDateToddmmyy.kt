package com.ekosp.indoweb.epesantren.helper

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

fun parseDateToddMMyyyy(time: String?): String? {
    val inputPattern = "yyyy-MM-dd"
    val outputPattern = "dd MMMM yyyy"
    val inputFormat = SimpleDateFormat(inputPattern)
    val outputFormat = SimpleDateFormat(outputPattern)
    var date: Date? = null
    var str: String? = null
    try {
        date = inputFormat.parse(time)
        str = outputFormat.format(date)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return str
}