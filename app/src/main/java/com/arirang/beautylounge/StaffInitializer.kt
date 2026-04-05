package com.arirang.beautylounge

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.Date

object StaffInitializer {

    private const val TAG = "StaffInitializer"
    private const val PREF_NAME = "arirang_prefs"
    private const val KEY_STAFF_INITIALIZED = "staff_initialized"

    private data class StaffData(
        val employeeId: String,
        val name: String,
        val email: String,
        val password: String,
        val title: String,
        val services: List<String>
    )

    private val staffList = listOf(
        StaffData("EMP001", "Amara Njeri", "amara.njeri@arirang.com", "Amara@123",
            "Senior Hair Stylist", listOf("Hair Services")),
        StaffData("EMP002", "Fatuma Wanjiku", "fatuma.wanjiku@arirang.com", "Fatuma@123",
            "Nail Technician", listOf("Nail Care")),
        StaffData("EMP003", "Zara Akinyi", "zara.akinyi@arirang.com", "Zara@123",
            "Makeup Artist", listOf("Makeup")),
        StaffData("EMP004", "Imani Chebet", "imani.chebet@arirang.com", "Imani@123",
            "Massage Therapist", listOf("Massage")),
        StaffData("EMP005", "Naomi Kamau", "naomi.kamau@arirang.com", "Naomi@123",
            "Facial Specialist", listOf("Facial Treatment")),
        StaffData("EMP006", "Yemi Odhiambo", "yemi.odhiambo@arirang.com", "Yemi@123",
            "Hair Stylist", listOf("Hair Services")),
        StaffData("EMP007", "Kendi Mwangi", "kendi.mwangi@arirang.com", "Kendi@123",
            "Nail Artist", listOf("Nail Care")),
        StaffData("EMP008", "Talia Otieno", "talia.otieno@arirang.com", "Talia@123",
            "Beauty Expert", listOf("Makeup", "Facial Treatment")),
        StaffData("EMP009", "Sasha Kimani", "sasha.kimani@arirang.com", "Sasha@123",
            "Wellness Therapist", listOf("Massage", "Facial Treatment")),
        StaffData("EMP010", "Grace Wambua", "grace.wambua@arirang.com", "Grace@123",
            "Multi-Specialist", listOf("Hair Services", "Nail Care", "Makeup"))
    )

    /**
     * Initializes all staff members in Firebase Authentication and Firestore.
     * Runs silently in the background. Uses SharedPreferences to avoid re-running
     * on every launch. Also checks Firestore as a secondary guard.
     */
    fun initializeStaff(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_STAFF_INITIALIZED, false)) {
            Log.d(TAG, "Staff already initialized, skipping")
            return
        }

        val db = FirebaseFirestore.getInstance()

        // Check Firestore first as a secondary guard against re-initialization
        db.collection("staffMembers").document(staffList.first().employeeId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d(TAG, "Staff already found in Firestore, marking initialized")
                    prefs.edit().putBoolean(KEY_STAFF_INITIALIZED, true).apply()
                } else {
                    Log.d(TAG, "Starting staff auto-registration")
                    registerStaffSequentially(context, db, 0)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Firestore check failed, proceeding with registration: ${e.message}")
                registerStaffSequentially(context, db, 0)
            }
    }

    private fun registerStaffSequentially(context: Context, db: FirebaseFirestore, index: Int) {
        if (index >= staffList.size) {
            Log.d(TAG, "All ${staffList.size} staff members initialized successfully")
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_STAFF_INITIALIZED, true).apply()
            return
        }

        val staff = staffList[index]
        val auth = FirebaseAuth.getInstance()

        auth.createUserWithEmailAndPassword(staff.email, staff.password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid == null) {
                    Log.e(TAG, "No UID returned for ${staff.name}, skipping")
                    // Sign out before continuing so we don't leave a staff account active
                    auth.signOut()
                    registerStaffSequentially(context, db, index + 1)
                    return@addOnSuccessListener
                }

                // Save user profile to 'users' collection (used by LoginActivity for role routing)
                val userData = hashMapOf(
                    "name" to staff.name,
                    "email" to staff.email,
                    "role" to "staff",
                    "employeeId" to staff.employeeId,
                    "title" to staff.title,
                    "createdAt" to Date()
                )

                // Save staff profile to 'staffMembers' collection (used by BookingActivity)
                val staffData = hashMapOf(
                    "name" to staff.name,
                    "employeeId" to staff.employeeId,
                    "email" to staff.email,
                    "title" to staff.title,
                    "services" to staff.services,
                    "status" to "active",
                    "createdAt" to Date()
                )

                db.collection("users").document(uid).set(userData)
                    .addOnSuccessListener {
                        db.collection("staffMembers").document(staff.employeeId).set(staffData)
                            .addOnSuccessListener {
                                Log.d(TAG, "Registered ${staff.name} (${staff.employeeId})")
                                // Sign out after each registration so no staff account remains
                                // active during the initialization process
                                auth.signOut()
                                registerStaffSequentially(context, db, index + 1)
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Failed to save staffMembers doc for ${staff.name}: ${e.message}")
                                auth.signOut()
                                registerStaffSequentially(context, db, index + 1)
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to save users doc for ${staff.name}: ${e.message}")
                        auth.signOut()
                        registerStaffSequentially(context, db, index + 1)
                    }
            }
            .addOnFailureListener { e ->
                if (e.message?.contains("email address is already in use") == true) {
                    // Staff already registered in Auth - ensure Firestore doc exists
                    Log.d(TAG, "${staff.name} already in Firebase Auth, updating staffMembers doc")
                    val staffData = hashMapOf(
                        "name" to staff.name,
                        "employeeId" to staff.employeeId,
                        "email" to staff.email,
                        "title" to staff.title,
                        "services" to staff.services,
                        "status" to "active",
                        "createdAt" to Date()
                    )
                    db.collection("staffMembers").document(staff.employeeId)
                        .set(staffData, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d(TAG, "Updated staffMembers doc for ${staff.name}")
                            registerStaffSequentially(context, db, index + 1)
                        }
                        .addOnFailureListener { ex ->
                            Log.e(TAG, "Failed to update staffMembers doc for ${staff.name}: ${ex.message}")
                            registerStaffSequentially(context, db, index + 1)
                        }
                } else {
                    Log.e(TAG, "Failed to register ${staff.name}: ${e.message}")
                    registerStaffSequentially(context, db, index + 1)
                }
            }
    }
}
