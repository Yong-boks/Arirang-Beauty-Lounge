package com.arirang.beautylounge

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arirang.beautylounge.databinding.ActivityStaffDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StaffDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStaffDashboardBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadUserData()
        setupClickListeners()
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "Staff"
                    val employeeId = document.getString("employeeId") ?: "N/A"
                    binding.tvWelcome.text = "Welcome, $name!"
                    binding.tvEmployeeId.text = "Employee ID: $employeeId"
                }
            }
        loadTodaySchedule(uid)
    }

    private fun loadTodaySchedule(uid: String) {
        val sdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
        val todayStr = sdf.format(Calendar.getInstance().time)

        db.collection("bookings")
            .whereEqualTo("staffId", uid)
            .whereEqualTo("date", todayStr)
            .get()
            .addOnSuccessListener { documents ->
                val count = documents.size()
                binding.tvScheduleInfo.text = if (count == 0)
                    "No appointments scheduled for today"
                else {
                    val lines = documents.mapNotNull { doc ->
                        val time = doc.getString("time") ?: return@mapNotNull null
                        val service = doc.getString("serviceName") ?: ""
                        val customer = doc.getString("customerName") ?: ""
                        "$time — $service ($customer)"
                    }.sorted()
                    lines.joinToString("\n• ", "• ")
                }
            }
            .addOnFailureListener {
                binding.tvScheduleInfo.text = "Could not load schedule"
            }

        db.collection("bookings")
            .whereEqualTo("staffId", uid)
            .get()
            .addOnSuccessListener { documents ->
                val uniqueCustomers = documents.mapNotNull { it.getString("customerId") }.toSet().size
                binding.tvCustomerCount.text = "$uniqueCustomers unique customer(s) assigned"
            }
            .addOnFailureListener {
                binding.tvCustomerCount.text = "Could not load customer data"
            }
    }

    private fun setupClickListeners() {
        binding.btnViewSchedule.setOnClickListener {
            startActivity(Intent(this, ManageScheduleActivity::class.java))
        }

        binding.btnViewCustomers.setOnClickListener {
            startActivity(Intent(this, StaffCustomersActivity::class.java))
        }

        binding.btnMyDuties.setOnClickListener {
            startActivity(Intent(this, MyDutiesActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, RoleSelectionActivity::class.java))
            finishAffinity()
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        // Prevent going back from dashboard
    }
}

