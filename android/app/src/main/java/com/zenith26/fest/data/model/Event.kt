package com.zenith26.fest.data.model

import com.google.gson.annotations.SerializedName

data class Event(
    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("description")
    val description: String? = "",

    @SerializedName("event_date")
    val eventDate: String,

    @SerializedName("event_time")
    val eventTime: String,

    @SerializedName("venue")
    val venue: String,

    @SerializedName("capacity")
    val capacity: Int,

    @SerializedName("fee")
    val fee: Int = 0,

    @SerializedName("icon")
    val icon: String? = "🎉",

    @SerializedName("available_seats")
    val availableSeats: Int
) {
    val isFull: Boolean
        get() = availableSeats <= 0

    val feeDisplay: String
        get() = if (fee > 0) "₹$fee" else "Free"

    val formattedDate: String
        get() {
            return try {
                val parts = eventDate.split("-")
                if (parts.size == 3) {
                    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                    val monthIndex = parts[1].toIntOrNull()?.minus(1) ?: return eventDate
                    val day = parts[2].toIntOrNull() ?: return eventDate
                    if (monthIndex in months.indices) {
                        "$day ${months[monthIndex]}"
                    } else eventDate
                } else eventDate
            } catch (e: Exception) {
                eventDate
            }
        }
}
