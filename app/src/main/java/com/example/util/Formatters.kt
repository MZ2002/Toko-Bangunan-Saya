package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {

    private val indonesianLocale = Locale("id", "ID")

    fun formatRupiah(amount: Double?): String {
        if (amount == null) return "-"
        val symbols = DecimalFormatSymbols(indonesianLocale).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val isWhole = amount % 1.0 == 0.0
        val pattern = if (isWhole) "Rp #,##0" else "Rp #,##0.##"
        val formatter = DecimalFormat(pattern, symbols)
        return formatter.format(amount)
    }

    fun formatNumber(number: Double?): String {
        if (number == null) return "0"
        val symbols = DecimalFormatSymbols(indonesianLocale).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val isWhole = number % 1.0 == 0.0
        val pattern = if (isWhole) "#,##0" else "#,##0.##"
        val formatter = DecimalFormat(pattern, symbols)
        return formatter.format(number)
    }

    fun formatPercentage(pct: Double?): String {
        if (pct == null) return "-"
        val symbols = DecimalFormatSymbols(indonesianLocale).apply {
            decimalSeparator = ','
            groupingSeparator = '.'
        }
        val isWhole = pct % 1.0 == 0.0
        val pattern = if (isWhole) "#,##0" else "#,##0.##"
        val formatter = DecimalFormat(pattern, symbols)
        return "${formatter.format(pct)}%"
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", indonesianLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", indonesianLocale)
        return sdf.format(Date(timestamp))
    }
}
