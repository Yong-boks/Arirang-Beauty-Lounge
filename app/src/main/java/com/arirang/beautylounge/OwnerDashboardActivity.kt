package com.arirang.beautylounge

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.arirang.beautylounge.databinding.ActivityOwnerDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OwnerDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOwnerDashboardBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOwnerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Prevent navigating back from the dashboard to the login/registration screens
        onBackPressedDispatcher.addCallback(this) { /* do nothing */ }

        loadOwnerData()
        loadStats()
        setupClickListeners()
    }

    private fun loadOwnerData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "Owner"
                    val salonCode = document.getString("salonCode") ?: "N/A"
                    binding.tvWelcome.text = "Welcome, $name!"
                    binding.tvSalonCode.text = "Salon Code: $salonCode"
                }
            }
    }

    private fun loadStats() {
        db.collection("users").whereEqualTo("role", "staff").get()
            .addOnSuccessListener { documents ->
                binding.tvStaffCount.text = "${documents.size()} Staff Members"
            }

        db.collection("users").whereEqualTo("role", "customer").get()
            .addOnSuccessListener { documents ->
                binding.tvCustomerCount.text = "${documents.size()} Customers"
            }

        db.collection("bookings").get()
            .addOnSuccessListener { documents ->
                binding.tvBookingCount.text = "${documents.size()} Total Bookings"
            }
    }

    private fun setupClickListeners() {
        binding.btnManageStaff.setOnClickListener {
            startActivity(Intent(this, OwnerManageStaffActivity::class.java))
        }

        binding.btnViewAllBookings.setOnClickListener {
            startActivity(Intent(this, OwnerAllBookingsActivity::class.java))
        }

        binding.btnInventory.setOnClickListener {
            startActivity(Intent(this, InventoryActivity::class.java))
        }

        binding.btnReports.setOnClickListener {
            startActivity(Intent(this, OwnerReportsActivity::class.java))
        }

        binding.btnManageSchedules.setOnClickListener {
            startActivity(Intent(this, OwnerAllBookingsActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, RoleSelectionActivity::class.java))
            finishAffinity()
        }
    }
}
