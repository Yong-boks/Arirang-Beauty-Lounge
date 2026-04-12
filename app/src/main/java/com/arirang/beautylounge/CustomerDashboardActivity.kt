package com.arirang.beautylounge

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.arirang.beautylounge.databinding.ActivityCustomerDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CustomerDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerDashboardBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Prevent navigating back from the dashboard to the login/registration screens
        onBackPressedDispatcher.addCallback(this) { /* do nothing */ }

        loadUserData()
        setupClickListeners()
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "Customer"
                    binding.tvWelcome.text = "Welcome, $name!"
                }
            }
    }

    private fun setupClickListeners() {
        binding.btnViewServices.setOnClickListener {
            startActivity(Intent(this, ServicesActivity::class.java))
        }

        binding.btnMyBookings.setOnClickListener {
            startActivity(Intent(this, MyBookingsActivity::class.java))
        }

        binding.btnChatbot.setOnClickListener {
            startActivity(Intent(this, ChatbotActivity::class.java))
        }

        binding.btnMyProfile.setOnClickListener {
            startActivity(Intent(this, MyProfileActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, RoleSelectionActivity::class.java))
            finishAffinity()
        }
    }
}
