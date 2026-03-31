package com.arirang.beautylounge

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthState()
        }, 2000)
    }

    private fun checkAuthState() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        when (document.getString("role")) {
                            "customer" -> navigateTo(CustomerDashboardActivity::class.java)
                            "staff" -> navigateTo(StaffDashboardActivity::class.java)
                            "owner" -> navigateTo(OwnerDashboardActivity::class.java)
                            else -> navigateTo(RoleSelectionActivity::class.java)
                        }
                    } else {
                        navigateTo(RoleSelectionActivity::class.java)
                    }
                }
                .addOnFailureListener {
                    navigateTo(RoleSelectionActivity::class.java)
                }
        } else {
            navigateTo(RoleSelectionActivity::class.java)
        }
    }

    private fun navigateTo(destination: Class<*>) {
        startActivity(Intent(this, destination))
        finish()
    }
}
