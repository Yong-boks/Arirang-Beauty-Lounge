package com.arirang.beautylounge

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arirang.beautylounge.databinding.ActivityStaffDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
        db.collection("schedules").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val tasks = document.get("tasks") as? List<String> ?: emptyList()
                    binding.tvScheduleInfo.text = if (tasks.isEmpty())
                        "No tasks scheduled for today"
                    else
                        tasks.joinToString("\n• ", "• ")
                } else {
                    binding.tvScheduleInfo.text = "No schedule found for today"
                }
            }
            .addOnFailureListener {
                binding.tvScheduleInfo.text = "Could not load schedule"
            }

        db.collection("bookings")
            .whereEqualTo("staffId", uid)
            .get()
            .addOnSuccessListener { documents ->
                val count = documents.size()
                binding.tvCustomerCount.text = "$count customer(s) assigned today"
            }
            .addOnFailureListener {
                binding.tvCustomerCount.text = "Could not load customer data"
            }
    }

    private fun setupClickListeners() {
        binding.btnViewSchedule.setOnClickListener {
            Toast.makeText(this, "Schedule Management - Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        binding.btnViewCustomers.setOnClickListener {
            Toast.makeText(this, "Customer List - Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        binding.btnMyDuties.setOnClickListener {
            Toast.makeText(this, "My Duties - Coming Soon!", Toast.LENGTH_SHORT).show()
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
