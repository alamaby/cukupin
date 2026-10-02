package com.alamaby.cukupin.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val idLocale = Locale("id", "ID")
private val shortDateFmt = DateTimeFormatter.ofPattern("d MMM yyyy", idLocale)
private val longDateFmt = DateTimeFormatter.ofPattern("d MMMM yyyy", idLocale)
private val dayFmt = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", idLocale)

fun LocalDate.formatShort(): String = format(shortDateFmt)
fun LocalDate.formatLong(): String = format(longDateFmt)
fun LocalDate.formatDay(): String = format(dayFmt)

/** Petakan iconKey kategori ke ikon Material. */
fun categoryIcon(iconKey: String): ImageVector = when (iconKey) {
    "restaurant" -> Icons.Filled.Restaurant
    "directions_car" -> Icons.Filled.DirectionsCar
    "shopping_bag" -> Icons.Filled.ShoppingBag
    "movie" -> Icons.Filled.Movie
    "health" -> Icons.Filled.HealthAndSafety
    "school" -> Icons.Filled.School
    "receipt" -> Icons.Filled.Receipt
    else -> Icons.Filled.Category
}
