package com.arirang.beautylounge

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.arirang.beautylounge.databinding.ItemStaffBinding

class StaffSelectionAdapter(
    private val staffList: List<StaffMember>,
    private val onStaffSelected: (StaffMember) -> Unit
) : RecyclerView.Adapter<StaffSelectionAdapter.StaffViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val binding = ItemStaffBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return StaffViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        holder.bind(staffList[position], position == selectedPosition)
        holder.itemView.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            val previousSelected = selectedPosition
            selectedPosition = pos
            notifyItemChanged(previousSelected)
            notifyItemChanged(selectedPosition)
            onStaffSelected(staffList[pos])
        }
    }

    override fun getItemCount() = staffList.size

    fun clearSelection() {
        val prev = selectedPosition
        selectedPosition = -1
        notifyItemChanged(prev)
    }

    class StaffViewHolder(private val binding: ItemStaffBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(staff: StaffMember, isSelected: Boolean) {
            binding.tvStaffName.text = staff.name
            binding.tvStaffSpecialization.text = staff.specialization
            binding.tvStaffId.text = staff.employeeId
            binding.tvStaffInitial.text = staff.name.firstOrNull()?.toString() ?: "S"
            binding.tvSelected.visibility = if (isSelected) android.view.View.VISIBLE else android.view.View.GONE

            val bgColor = if (isSelected)
                ContextCompat.getColor(binding.root.context, R.color.colorCustomerCard)
            else
                ContextCompat.getColor(binding.root.context, R.color.colorWhite)
            binding.cardStaff.setCardBackgroundColor(bgColor)

            val strokeColor = if (isSelected)
                ContextCompat.getColor(binding.root.context, R.color.colorPrimary)
            else
                ContextCompat.getColor(binding.root.context, R.color.colorLightGrey)
            binding.cardStaff.setStrokeColor(strokeColor)
        }
    }
}
