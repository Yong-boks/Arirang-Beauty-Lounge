package com.arirang.beautylounge

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arirang.beautylounge.databinding.ActivityRoleSelectionBinding

class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardCustomer.setOnClickListener {
            navigateToLogin("customer")
        }

        binding.cardStaff.setOnClickListener {
            navigateToLogin("staff")
        }

        binding.cardOwner.setOnClickListener {
            navigateToLogin("owner")
        }
    }

    private fun navigateToLogin(role: String) {
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("role", role)
        startActivity(intent)
    }
}
