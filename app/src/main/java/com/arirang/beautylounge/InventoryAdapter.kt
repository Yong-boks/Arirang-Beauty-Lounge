package com.arirang.beautylounge

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arirang.beautylounge.databinding.ItemInventoryBinding

class InventoryAdapter(
    private val items: List<InventoryItem>,
    private val onEditClick: (InventoryItem) -> Unit,
    private val onDeleteClick: (InventoryItem) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInventoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onEditClick, onDeleteClick)
    }

    override fun getItemCount() = items.size

    class ViewHolder(private val binding: ItemInventoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: InventoryItem,
            onEditClick: (InventoryItem) -> Unit,
            onDeleteClick: (InventoryItem) -> Unit
        ) {
            binding.tvItemName.text = item.name
            binding.tvItemQuantity.text = "${item.quantity} ${item.unit}"

            if (item.isLowStock) {
                binding.tvLowStockWarning.visibility = android.view.View.VISIBLE
                binding.viewStockDot.setBackgroundColor(0xFFB71C1C.toInt())
            } else {
                binding.tvLowStockWarning.visibility = android.view.View.GONE
                binding.viewStockDot.setBackgroundColor(0xFF2E7D32.toInt())
            }

            binding.btnEdit.setOnClickListener { onEditClick(item) }
            binding.btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }
}
