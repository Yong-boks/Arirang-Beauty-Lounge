package com.arirang.beautylounge

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arirang.beautylounge.databinding.ActivityMyProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "My Profile"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadProfile()

        binding.btnSaveProfile.setOnClickListener { saveProfile() }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, RoleSelectionActivity::class.java))
            finishAffinity()
        }
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: ""
                    val email = document.getString("email") ?: auth.currentUser?.email ?: ""
                    val phone = document.getString("phone") ?: ""

                    binding.tvProfileName.text = name.ifEmpty { "Customer" }
                    binding.tvProfileEmail.text = email
                    binding.etName.setText(name)
                    binding.etPhone.setText(phone)

                    val createdAt = document.getTimestamp("createdAt")
                    if (createdAt != null) {
                        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        binding.tvMemberSince.text = "Member since ${sdf.format(createdAt.toDate())}"
                    } else {
                        binding.tvMemberSince.text = ""
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Could not load profile", Toast.LENGTH_SHORT).show()
            }

        loadBookingStats(uid)
    }

    private fun loadBookingStats(uid: String) {
        db.collection("bookings").whereEqualTo("customerId", uid).get()
            .addOnSuccessListener { documents ->
                val total = documents.size()
                val upcoming = documents.count { doc ->
                    val status = doc.getString("status") ?: "Confirmed"
                    status == "Confirmed"
                }
                binding.tvTotalBookings.text = total.toString()
                binding.tvActiveBookings.text = upcoming.toString()
            }
            .addOnFailureListener {
                binding.tvTotalBookings.text = "—"
                binding.tvActiveBookings.text = "—"
            }
    }

    private fun saveProfile() {
        val name = binding.etName.text?.toString()?.trim() ?: ""
        val phone = binding.etPhone.text?.toString()?.trim() ?: ""

        if (name.isEmpty()) {
            binding.etName.error = "Name is required"
            binding.etName.requestFocus()
            return
        }

        val uid = auth.currentUser?.uid ?: return
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSaveProfile.isEnabled = false

        val updates = hashMapOf<String, Any>(
            "name" to name,
            "phone" to phone,
            "updatedAt" to Date()
        )

        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                binding.btnSaveProfile.isEnabled = true
                binding.tvProfileName.text = name
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnSaveProfile.isEnabled = true
                Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
