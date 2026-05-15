package com.yourname.raktavahini.data

import java.text.SimpleDateFormat
import java.util.*

data class Donor(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val bloodGroup: String = "",
    val location: String = "",
    val phone: String = "",
    val lastDonationDate: String = "",
    val isEligible: Boolean = true
)

data class DonationRecord(
    val id: String = UUID.randomUUID().toString(),
    val date: String = "",
    val location: String = "",
    val note: String = ""
)

data class BloodRequest(
    val id: String = UUID.randomUUID().toString(),
    val bloodGroup: String = "",
    val hospital: String = "",
    val city: String = "",
    val contactName: String = "",
    val contactPhone: String = "",
    val urgency: String = "Normal",
    val timestamp: String = todayString(),
    val postedByUid: String = ""
)

data class AppStats(
    val totalDonors: Int = 0,
    val totalRequests: Int = 0
)

val BLOOD_GROUPS = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
val URGENCY_LEVELS = listOf("Critical", "Urgent", "Normal")

fun isDonorEligible(lastDonationDate: String): Boolean {
    if (lastDonationDate.isBlank()) return true
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val last = sdf.parse(lastDonationDate) ?: return true
        val diff = Date().time - last.time
        (diff / (1000 * 60 * 60 * 24)) >= 90
    } catch (e: Exception) { true }
}

fun daysUntilEligible(lastDonationDate: String): Int {
    if (lastDonationDate.isBlank()) return 0
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val last = sdf.parse(lastDonationDate) ?: return 0
        val diff = Date().time - last.time
        val daysDonated = (diff / (1000 * 60 * 60 * 24)).toInt()
        maxOf(0, 90 - daysDonated)
    } catch (e: Exception) { 0 }
}

fun todayString(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())