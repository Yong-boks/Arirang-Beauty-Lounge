package com.arirang.beautylounge

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.arirang.beautylounge.databinding.ActivityInventoryBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class InventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryBinding
    private lateinit var db: FirebaseFirestore

    private val inventoryList = mutableListOf<InventoryItem>()
    private lateinit var adapter: InventoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Inventory Management"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        db = FirebaseFirestore.getInstance()

        adapter = InventoryAdapter(
            inventoryList,
            onEditClick = { item -> showAddEditDialog(item) },
            onDeleteClick = { item -> confirmDelete(item) }
        )
        binding.rvInventory.layoutManager = LinearLayoutManager(this)
        binding.rvInventory.adapter = adapter

        binding.fabAddItem.setOnClickListener { showAddEditDialog(null) }

        loadInventory()
    }

    private fun loadInventory() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvInventory.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE

        db.collection("inventory")
            .orderBy("name")
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE
                inventoryList.clear()

                for (doc in documents) {
                    inventoryList.add(
                        InventoryItem(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            quantity = (doc.getLong("quantity") ?: 0L).toInt(),
                            unit = doc.getString("unit") ?: "units",
                            lowStockThreshold = (doc.getLong("lowStockThreshold") ?: 5L).toInt(),
                            updatedAt = doc.getLong("updatedAt") ?: 0L
                        )
                    )
                }

                val total = inventoryList.size
                val lowStock = inventoryList.count { it.isLowStock }
                binding.tvInventorySummary.text = buildString {
                    append("$total item(s) tracked")
                    if (lowStock > 0) append(" • ⚠️ $lowStock low stock")
                }

                if (inventoryList.isEmpty()) {
                    binding.layoutEmpty.visibility = View.VISIBLE
                } else {
                    binding.rvInventory.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.tvInventorySummary.text = "Could not load inventory"
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showAddEditDialog(existing: InventoryItem?) {
        val isEdit = existing != null

        val dialogView = layoutInflater.inflate(
            android.R.layout.simple_list_item_1, null
        )

        // Build a simple form programmatically
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            val pad = (16 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, 0)
        }

        fun makeField(hint: String, value: String, inputType: Int): TextInputEditText {
            val til = TextInputLayout(this, null,
                com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox
            ).apply {
                this.hint = hint
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = (12 * resources.displayMetrics.density).toInt() }
            }
            val et = TextInputEditText(til.context).apply {
                setText(value)
                this.inputType = inputType
            }
            til.addView(et)
            layout.addView(til)
            return et
        }

        val etName = makeField("Product Name", existing?.name ?: "",
            android.text.InputType.TYPE_CLASS_TEXT)
        val etQuantity = makeField("Quantity", if (isEdit) existing!!.quantity.toString() else "",
            android.text.InputType.TYPE_CLASS_NUMBER)
        val etUnit = makeField("Unit (e.g. bottles, ml, pcs)", existing?.unit ?: "pcs",
            android.text.InputType.TYPE_CLASS_TEXT)
        val etThreshold = makeField("Low Stock Threshold", if (isEdit) existing!!.lowStockThreshold.toString() else "5",
            android.text.InputType.TYPE_CLASS_NUMBER)

        AlertDialog.Builder(this)
            .setTitle(if (isEdit) "Edit Item" else "Add New Item")
            .setView(layout)
            .setPositiveButton(if (isEdit) "Save" else "Add") { _, _ ->
                val name = etName.text.toString().trim()
                val quantityStr = etQuantity.text.toString().trim()
                val unit = etUnit.text.toString().trim().ifEmpty { "pcs" }
                val thresholdStr = etThreshold.text.toString().trim()

                if (name.isEmpty() || quantityStr.isEmpty()) {
                    Toast.makeText(this, "Name and quantity are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val quantity = quantityStr.toIntOrNull() ?: 0
                val threshold = thresholdStr.toIntOrNull() ?: 5

                val data = hashMapOf<String, Any>(
                    "name" to name,
                    "quantity" to quantity,
                    "unit" to unit,
                    "lowStockThreshold" to threshold,
                    "updatedAt" to Date().time
                )

                if (isEdit) {
                    db.collection("inventory").document(existing!!.id).update(data)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show()
                            loadInventory()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    db.collection("inventory").add(data)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show()
                            loadInventory()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDelete(item: InventoryItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete Item")
            .setMessage("Remove \"${item.name}\" from inventory?")
            .setPositiveButton("Delete") { _, _ ->
                db.collection("inventory").document(item.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "\"${item.name}\" removed", Toast.LENGTH_SHORT).show()
                        loadInventory()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
