package com.arirang.beautylounge

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.arirang.beautylounge.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var selectedRole: String = "customer"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        selectedRole = intent.getStringExtra("role") ?: "customer"
        updateRoleUI()

        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.btnRegister.setOnClickListener {
            navigateToRegistration()
        }

        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }

        binding.tvBackToRoleSelection.setOnClickListener {
            startActivity(Intent(this, RoleSelectionActivity::class.java))
            finish()
        }

        onBackPressedDispatcher.addCallback(this) {
            startActivity(Intent(this@LoginActivity, RoleSelectionActivity::class.java))
            finish()
        }
    }

    private fun updateRoleUI() {
        val roleLabel = when (selectedRole) {
            "staff" -> "Staff"
            "owner" -> "Owner"
            else -> "Customer"
        }
        binding.tvRoleTitle.text = "$roleLabel Login"
        binding.tvRegisterPrompt.text = "Don't have an account? Register as $roleLabel"
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Please enter a valid email address"
            binding.tilEmail.requestFocus()
            return
        }
        binding.tilEmail.error = null

        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                db.collection("users").document(uid).get()
                    .addOnSuccessListener { document ->
                        binding.progressBar.visibility = View.GONE
                        if (document.exists()) {
                            when (document.getString("role")) {
                                "customer" -> navigateToDashboard(CustomerDashboardActivity::class.java)
                                "staff" -> navigateToDashboard(StaffDashboardActivity::class.java)
                                "owner" -> navigateToDashboard(OwnerDashboardActivity::class.java)
                                else -> navigateToDashboard(CustomerDashboardActivity::class.java)
                            }
                        } else {
                            Toast.makeText(this, "User data not found. Please register.", Toast.LENGTH_SHORT).show()
                            binding.btnLogin.isEnabled = true
                        }
                    }
                    .addOnFailureListener { e ->
                        binding.progressBar.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showForgotPasswordDialog() {
        val emailInput = com.google.android.material.textfield.TextInputEditText(this)
        emailInput.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        emailInput.hint = "Enter your email address"

        val container = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            val pad = (16 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, 0)
            addView(emailInput)
        }

        AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setMessage("Enter your account email to receive a password reset link.")
            .setView(container)
            .setPositiveButton("Send Link") { _, _ ->
                val email = emailInput.text.toString().trim()
                if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Password reset link sent to $email",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToRegistration() {
        val destination = when (selectedRole) {
            "staff" -> StaffRegistrationActivity::class.java
            "owner" -> OwnerRegistrationActivity::class.java
            else -> CustomerRegistrationActivity::class.java
        }
        startActivity(Intent(this, destination))
    }

    private fun navigateToDashboard(destination: Class<*>) {
        startActivity(Intent(this, destination))
        finishAffinity()
    }
}
