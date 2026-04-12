package com.arirang.beautylounge

import java.util.Calendar

/**
 * Generates 30-minute appointment time slots for a given day.
 *
 * Business hours:
 *   Monday – Saturday: 08:00 – 18:00
 *   Sunday:            09:00 – 15:00
 *
 * Returns a list of time strings formatted as "hh:mm AM/PM" (e.g. "08:00 AM", "02:30 PM").
 */
object TimeSlotUtils {

    fun generateSlots(calendar: Calendar): List<String> {
        val isSunday = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
        val startHour = if (isSunday) 9 else 8
        val endHour = if (isSunday) 15 else 18

        val slots = mutableListOf<String>()
        var h = startHour
        var m = 0
        while (h < endHour) {
            val amPm = if (h < 12) "AM" else "PM"
            val h12 = when {
                h == 0 -> 12
                h > 12 -> h - 12
                else -> h
            }
            slots.add(String.format("%02d:%02d %s", h12, m, amPm))
            m += 30
            if (m >= 60) {
                m = 0
                h++
            }
        }
        return slots
    }
}
