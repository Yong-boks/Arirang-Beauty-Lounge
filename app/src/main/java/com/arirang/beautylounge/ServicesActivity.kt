package com.arirang.beautylounge

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arirang.beautylounge.databinding.ActivityServicesBinding

class ServicesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServicesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Our Services"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnBookAppointment.setOnClickListener {
            startActivity(Intent(this, BookingActivity::class.java))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
