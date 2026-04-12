package com.arirang.beautylounge

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.Date

/**
 * Seeds the `staffMembers` Firestore collection with the 10 predefined staff profiles.
 * This collection acts as the authoritative whitelist of valid employee IDs.
 *
 * No Firebase Auth accounts are created here. Staff members register themselves via
 * [StaffRegistrationActivity], which validates their entered employee ID against this
 * collection before creating an account.
 */
object StaffInitializer {

    private const val TAG = "StaffInitializer"
    private const val PREF_NAME = "arirang_prefs"
    private const val KEY_STAFF_INITIALIZED = "staff_initialized"

    private data class StaffProfile(
        val employeeId: String,
        val name: String,
        val email: String,
        val title: String,
        val services: List<String>
    )

    private val staffProfiles = listOf(
        StaffProfile("EMP001", "Amara Njeri",    "amara.njeri@arirang.com",    "Senior Hair Stylist",  listOf("Hair Services")),
        StaffProfile("EMP002", "Fatuma Wanjiku", "fatuma.wanjiku@arirang.com", "Nail Technician",      listOf("Nail Care")),
        StaffProfile("EMP003", "Zara Akinyi",    "zara.akinyi@arirang.com",    "Makeup Artist",        listOf("Makeup")),
        StaffProfile("EMP004", "Imani Chebet",   "imani.chebet@arirang.com",   "Massage Therapist",    listOf("Massage")),
        StaffProfile("EMP005", "Naomi Kamau",    "naomi.kamau@arirang.com",    "Facial Specialist",    listOf("Facial Treatment")),
        StaffProfile("EMP006", "Yemi Odhiambo",  "yemi.odhiambo@arirang.com",  "Hair Stylist",         listOf("Hair Services")),
        StaffProfile("EMP007", "Kendi Mwangi",   "kendi.mwangi@arirang.com",   "Nail Artist",          listOf("Nail Care")),
        StaffProfile("EMP008", "Talia Otieno",   "talia.otieno@arirang.com",   "Beauty Expert",        listOf("Makeup", "Facial Treatment")),
        StaffProfile("EMP009", "Sasha Kimani",   "sasha.kimani@arirang.com",   "Wellness Therapist",   listOf("Massage", "Facial Treatment")),
        StaffProfile("EMP010", "Grace Wambua",   "grace.wambua@arirang.com",   "Multi-Specialist",     listOf("Hair Services", "Nail Care", "Makeup"))
    )

    /**
     * Seeds the `staffMembers` Firestore collection on first launch.
     * Uses SharedPreferences + a Firestore existence check to avoid re-seeding.
     * Runs silently in the background.
     */
    fun initializeStaff(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_STAFF_INITIALIZED, false)) {
            Log.d(TAG, "Staff profiles already seeded, skipping")
            return
        }

        val db = FirebaseFirestore.getInstance()

        // Secondary guard: check Firestore before writing
        db.collection("staffMembers").document(staffProfiles.first().employeeId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d(TAG, "Staff profiles already found in Firestore, marking initialized")
                    prefs.edit().putBoolean(KEY_STAFF_INITIALIZED, true).apply()
                } else {
                    Log.d(TAG, "Seeding staff profiles to Firestore")
                    seedStaffProfiles(context, db)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Firestore check failed, proceeding with seeding: ${e.message}")
                seedStaffProfiles(context, db)
            }
    }

    private fun seedStaffProfiles(context: Context, db: FirebaseFirestore) {
        val batch = db.batch()
        for (profile in staffProfiles) {
            val ref = db.collection("staffMembers").document(profile.employeeId)
            val data = hashMapOf(
                "name" to profile.name,
                "employeeId" to profile.employeeId,
                "email" to profile.email,
                "title" to profile.title,
                "services" to profile.services,
                "status" to "active",
                "createdAt" to Date()
            )
            batch.set(ref, data, SetOptions.merge())
        }

        batch.commit()
            .addOnSuccessListener {
                Log.d(TAG, "Seeded ${staffProfiles.size} staff profiles successfully")
                val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                prefs.edit().putBoolean(KEY_STAFF_INITIALIZED, true).apply()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to seed staff profiles: ${e.message}")
            }
    }
}
