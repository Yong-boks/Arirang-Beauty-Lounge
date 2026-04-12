package com.arirang.beautylounge

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arirang.beautylounge.databinding.ActivityStaffRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class StaffRegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStaffRegistrationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        supportActionBar?.title = "Staff Registration"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnRegister.setOnClickListener {
            performRegistration()
        }
    }

    private fun performRegistration() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val employeeId = binding.etEmployeeId.text.toString().trim().uppercase(java.util.Locale.ROOT)

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty() || employeeId.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false

        // Step 1: Verify the employee ID exists in the staffMembers whitelist
        db.collection("staffMembers").document(employeeId).get()
            .addOnSuccessListener { staffDoc ->
                if (!staffDoc.exists()) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    Toast.makeText(
                        this,
                        "Employee ID '$employeeId' not found. Please check your ID or contact your manager.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }

                // Step 2: Check the employee ID is not already registered
                db.collection("users").whereEqualTo("employeeId", employeeId).get()
                    .addOnSuccessListener { existingUsers ->
                        if (!existingUsers.isEmpty) {
                            binding.progressBar.visibility = View.GONE
                            binding.btnRegister.isEnabled = true
                            Toast.makeText(
                                this,
                                "An account for Employee ID '$employeeId' already exists. Please log in instead.",
                                Toast.LENGTH_LONG
                            ).show()
                            return@addOnSuccessListener
                        }

                        // Step 3: All checks passed – create the Firebase Auth account
                        createStaffAccount(name, email, password, phone, employeeId)
                    }
                    .addOnFailureListener { e ->
                        binding.progressBar.visibility = View.GONE
                        binding.btnRegister.isEnabled = true
                        Toast.makeText(this, "Verification failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnRegister.isEnabled = true
                Toast.makeText(this, "Could not verify Employee ID: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createStaffAccount(
        name: String,
        email: String,
        password: String,
        phone: String,
        employeeId: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val userData = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "phone" to phone,
                    "role" to "staff",
                    "employeeId" to employeeId,
                    "createdAt" to Date()
                )
                db.collection("users").document(uid).set(userData)
                    .addOnSuccessListener {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, StaffDashboardActivity::class.java))
                        finishAffinity()
                    }
                    .addOnFailureListener { e ->
                        binding.progressBar.visibility = View.GONE
                        binding.btnRegister.isEnabled = true
                        Toast.makeText(this, "Error saving data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnRegister.isEnabled = true
                Toast.makeText(this, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
