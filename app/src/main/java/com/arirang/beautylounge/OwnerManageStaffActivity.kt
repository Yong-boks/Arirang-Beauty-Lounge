package com.arirang.beautylounge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arirang.beautylounge.databinding.ActivityOwnerManageStaffBinding
import com.arirang.beautylounge.databinding.ItemOwnerStaffBinding
import com.google.firebase.firestore.FirebaseFirestore

class OwnerManageStaffActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOwnerManageStaffBinding
    private lateinit var db: FirebaseFirestore

    data class StaffUser(
        val name: String,
        val employeeId: String,
        val phone: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOwnerManageStaffBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Manage Staff"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        db = FirebaseFirestore.getInstance()

        binding.rvStaff.layoutManager = LinearLayoutManager(this)
        loadStaff()
    }

    private fun loadStaff() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvStaff.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE

        db.collection("users").whereEqualTo("role", "staff").get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE
                val staffList = mutableListOf<StaffUser>()

                for (doc in documents) {
                    staffList.add(
                        StaffUser(
                            name = doc.getString("name") ?: "Unknown",
                            employeeId = doc.getString("employeeId") ?: "—",
                            phone = doc.getString("phone") ?: "—"
                        )
                    )
                }

                staffList.sortBy { it.name }

                val count = staffList.size
                binding.tvStaffSummary.text = if (count == 0)
                    "No staff registered yet"
                else
                    "$count staff member(s)"

                if (staffList.isEmpty()) {
                    binding.layoutEmpty.visibility = View.VISIBLE
                } else {
                    binding.rvStaff.visibility = View.VISIBLE
                    binding.rvStaff.adapter = StaffListAdapter(staffList)
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.tvStaffSummary.text = "Could not load staff"
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private class StaffListAdapter(private val staff: List<StaffUser>) :
        RecyclerView.Adapter<StaffListAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemOwnerStaffBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(staff[position])
        }

        override fun getItemCount() = staff.size

        class ViewHolder(private val binding: ItemOwnerStaffBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(staffUser: StaffUser) {
                binding.tvOwnerStaffName.text = staffUser.name
                binding.tvOwnerStaffEmployeeId.text = staffUser.employeeId
                binding.tvOwnerStaffPhone.text = staffUser.phone
                binding.tvOwnerStaffInitial.text =
                    staffUser.name.firstOrNull()?.toString() ?: "S"
            }
        }
    }
}
