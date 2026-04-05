package com.arirang.beautylounge

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.content.ContextCompat
import com.arirang.beautylounge.databinding.ActivityBookingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BookingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var currentStep = 0
    private var selectedService: Service? = null
    private var selectedStaff: StaffMember? = null
    private var selectedDate = ""
    private var selectedTime = ""
    private var customerName = ""

    private lateinit var staffAdapter: StaffSelectionAdapter
    private val filteredStaff = mutableListOf<StaffMember>()

    private val categories = listOf(
        "Hair Services", "Nail Care", "Facial Treatment", "Makeup", "Massage"
    )

    private val allServices = listOf(
        Service("haircut", "Haircut", "Hair Services", 1000, 20, 30),
        Service("hair_color", "Hair Color", "Hair Services", 3000, 60, 150),
        Service("blowout", "Blowout", "Hair Services", 1200, 30, 90),
        Service("braids_short", "Braids - Short", "Hair Services", 1500, 90, 120),
        Service("braids_medium", "Braids - Medium", "Hair Services", 2000, 120, 180),
        Service("braids_long", "Braids - Long", "Hair Services", 2500, 180, 240),
        Service("manicure", "Manicure", "Nail Care", 1000, 60, 180),
        Service("pedicure", "Pedicure", "Nail Care", 1200, 60, 180),
        Service("gel_nails", "Gel Nails", "Nail Care", 2000, 120, 180),
        Service("acrylics", "Acrylics", "Nail Care", 2500, 180, 240),
        Service("gem_gel", "Gem Gel", "Nail Care", 3000, 180, 240),
        Service("nail_art", "Nail Art (per finger)", "Nail Care", 300, 30, 60),
        Service("basic_facial", "Basic Facial", "Facial Treatment", 1500, 60, 90),
        Service("deep_cleansing", "Deep Cleansing Facial", "Facial Treatment", 2500, 120, 150),
        Service("full_glam", "Full Glam Makeup", "Makeup", 2500, 60, 120),
        Service("soft_makeup", "Soft Makeup", "Makeup", 1500, 30, 60),
        Service("bridal_makeup", "Bridal Makeup", "Makeup", 3500, 120, 180),
        Service("party_makeup", "Party Makeup", "Makeup", 2000, 60, 150),
        Service("swedish_massage", "Swedish Massage", "Massage", 2000, 60, 90),
        Service("deep_tissue", "Deep Tissue Massage", "Massage", 2500, 90, 120)
    )

    private val allStaff = listOf(
        // staffId: Firestore document ID used for booking lookups
        // employeeId: Human-readable display ID shown to customers
        StaffMember("STAFF_EMP001", "Amara Njeri", "EMP001",
            listOf("Hair Services"), "Senior Hair Stylist"),
        StaffMember("STAFF_EMP002", "Fatuma Wanjiku", "EMP002",
            listOf("Nail Care"), "Nail Technician"),
        StaffMember("STAFF_EMP003", "Zara Akinyi", "EMP003",
            listOf("Makeup"), "Makeup Artist"),
        StaffMember("STAFF_EMP004", "Imani Chebet", "EMP004",
            listOf("Massage"), "Massage Therapist"),
        StaffMember("STAFF_EMP005", "Naomi Kamau", "EMP005",
            listOf("Facial Treatment"), "Facial Specialist"),
        StaffMember("STAFF_EMP006", "Yemi Odhiambo", "EMP006",
            listOf("Hair Services"), "Hair Stylist"),
        StaffMember("STAFF_EMP007", "Kendi Mwangi", "EMP007",
            listOf("Nail Care"), "Nail Artist"),
        StaffMember("STAFF_EMP008", "Talia Otieno", "EMP008",
            listOf("Makeup", "Facial Treatment"), "Beauty Expert"),
        StaffMember("STAFF_EMP009", "Sasha Kimani", "EMP009",
            listOf("Massage", "Facial Treatment"), "Wellness Therapist"),
        StaffMember("STAFF_EMP010", "Grace Wambua", "EMP010",
            listOf("Hair Services", "Nail Care", "Makeup"), "Multi-Specialist")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Book a Service"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadCustomerName()
        setupServiceSelection()
        setupStaffRecyclerView()
        setupDateSelection()
        setupNavigation()
        updateStepUI()
    }

    private fun loadCustomerName() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                customerName = document.getString("name") ?: "Customer"
            }
    }

    private fun setupServiceSelection() {
        val categoryAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, categories
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter

        binding.spinnerCategory.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, pos: Int, id: Long
                ) {
                    updateServicesForCategory(categories[pos])
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        updateServicesForCategory(categories[0])
    }

    private fun updateServicesForCategory(category: String) {
        val servicesInCategory = allServices.filter { it.category == category }
        val serviceNames = servicesInCategory.map { it.name }
        val serviceAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, serviceNames
        )
        serviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerService.adapter = serviceAdapter

        binding.spinnerService.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, pos: Int, id: Long
                ) {
                    selectedService = servicesInCategory[pos]
                    refreshServiceDetails()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        if (servicesInCategory.isNotEmpty()) {
            selectedService = servicesInCategory[0]
            refreshServiceDetails()
        }
    }

    private fun refreshServiceDetails() {
        val service = selectedService ?: return
        binding.tvServicePrice.text = "KES ${service.price}"
        binding.tvServiceDuration.text = "${service.durationMin}–${service.durationMax} min"
    }

    private fun setupStaffRecyclerView() {
        staffAdapter = StaffSelectionAdapter(filteredStaff) { staff ->
            selectedStaff = staff
        }
        binding.rvStaff.layoutManager = LinearLayoutManager(this)
        binding.rvStaff.adapter = staffAdapter
    }

    private fun refreshStaffForService() {
        val category = selectedService?.category ?: return
        filteredStaff.clear()
        filteredStaff.addAll(allStaff.filter { it.services.contains(category) })
        staffAdapter.clearSelection()
        staffAdapter.notifyDataSetChanged()
        selectedStaff = null
    }

    private fun setupDateSelection() {
        binding.btnSelectDate.setOnClickListener { showDatePickerDialog() }
    }

    private fun showDatePickerDialog() {
        val today = Calendar.getInstance()
        val maxCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 14) }

        val picker = DatePickerDialog(
            this,
            { _, year, month, day ->
                val selected = Calendar.getInstance().apply { set(year, month, day) }
                val sdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
                selectedDate = sdf.format(selected.time)
                binding.tvSelectedDate.text = selectedDate
                populateTimeSlots(selected)
            },
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        )

        picker.datePicker.minDate = today.timeInMillis
        picker.datePicker.maxDate = maxCal.timeInMillis
        picker.show()
    }

    private fun populateTimeSlots(calendar: Calendar) {
        val isSunday = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
        val startHour = if (isSunday) 9 else 8
        val endHour = if (isSunday) 15 else 18

        val slots = mutableListOf<String>()
        var h = startHour
        var m = 0
        while (h < endHour) {
            val amPm = if (h < 12) "AM" else "PM"
            val h12 = when {
                h == 0 -> 12
                h > 12 -> h - 12
                else -> h
            }
            slots.add(String.format("%02d:%02d %s", h12, m, amPm))
            m += 30
            if (m >= 60) { m = 0; h++ }
        }

        val slotAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, slots)
        slotAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTime.adapter = slotAdapter
        binding.spinnerTime.visibility = View.VISIBLE
        binding.tvNoDateSelected.visibility = View.GONE

        binding.spinnerTime.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, pos: Int, id: Long
                ) {
                    selectedTime = slots[pos]
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        if (slots.isNotEmpty()) selectedTime = slots[0]
    }

    private fun setupNavigation() {
        binding.btnPrevious.setOnClickListener {
            if (currentStep > 0) {
                currentStep--
                updateStepUI()
            }
        }

        binding.btnNext.setOnClickListener {
            if (!validateStep()) return@setOnClickListener

            if (currentStep < 3) {
                if (currentStep == 0) refreshStaffForService()
                currentStep++
                updateStepUI()
                if (currentStep == 3) populateConfirmationSummary()
            } else {
                saveBooking()
            }
        }
    }

    private fun validateStep(): Boolean {
        return when (currentStep) {
            0 -> {
                if (selectedService == null) {
                    Toast.makeText(this, "Please select a service", Toast.LENGTH_SHORT).show()
                    false
                } else true
            }
            1 -> {
                if (selectedStaff == null) {
                    Toast.makeText(this, "Please select a staff member", Toast.LENGTH_SHORT).show()
                    false
                } else true
            }
            2 -> {
                when {
                    selectedDate.isEmpty() -> {
                        Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
                        false
                    }
                    selectedTime.isEmpty() -> {
                        Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show()
                        false
                    }
                    else -> true
                }
            }
            else -> true
        }
    }

    private fun updateStepUI() {
        binding.viewFlipper.displayedChild = currentStep

        val stepTitles = listOf(
            "Step 1 of 4: Select Service",
            "Step 2 of 4: Choose Stylist",
            "Step 3 of 4: Pick Date & Time",
            "Step 4 of 4: Confirm Booking"
        )
        binding.tvStepIndicator.text = stepTitles[currentStep]

        val dots = listOf(binding.step1Dot, binding.step2Dot, binding.step3Dot, binding.step4Dot)
        dots.forEachIndexed { index, dot ->
            dot.setBackgroundColor(
                if (index <= currentStep)
                    ContextCompat.getColor(this, R.color.colorWhite)
                else
                    android.graphics.Color.parseColor("#88FFFFFF")
            )
        }

        binding.btnPrevious.visibility = if (currentStep == 0) View.INVISIBLE else View.VISIBLE
        binding.btnNext.text = if (currentStep == 3) "Confirm Booking ✅" else "Next →"
    }

    private fun populateConfirmationSummary() {
        val service = selectedService ?: return
        val staff = selectedStaff ?: return
        binding.tvConfirmService.text = "Service: ${service.name}"
        binding.tvConfirmCategory.text = "Category: ${service.category}"
        binding.tvConfirmPrice.text = "Price: KES ${service.price}"
        binding.tvConfirmDuration.text = "Duration: ${service.durationMin}–${service.durationMax} min"
        binding.tvConfirmStaff.text = "Stylist: ${staff.name} (${staff.specialization})"
        binding.tvConfirmDate.text = "Date: $selectedDate"
        binding.tvConfirmTime.text = "Time: $selectedTime"
        binding.tvConfirmStatus.text = "Status: Confirmed ✅"
    }

    private fun saveBooking() {
        val uid = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }
        val service = selectedService ?: return
        val staff = selectedStaff ?: return

        binding.btnNext.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        val bookingId = db.collection("bookings").document().id
        val bookingData = hashMapOf(
            "bookingId" to bookingId,
            "customerId" to uid,
            "customerName" to customerName,
            "serviceId" to service.id,
            "serviceName" to service.name,
            "serviceCategory" to service.category,
            "staffId" to staff.staffId,
            "staffName" to staff.name,
            "date" to selectedDate,
            "time" to selectedTime,
            "price" to service.price,
            "durationMin" to service.durationMin,
            "durationMax" to service.durationMax,
            "status" to "Confirmed",
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("bookings").document(bookingId).set(bookingData)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "🎉 Booking confirmed!", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, MyBookingsActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnNext.isEnabled = true
                Toast.makeText(this, "Failed to save booking: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
