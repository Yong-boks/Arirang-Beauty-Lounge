package com.arirang.beautylounge

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arirang.beautylounge.databinding.ItemScheduleBinding

class StaffScheduleAdapter(private val bookings: List<Booking>) :
    RecyclerView.Adapter<StaffScheduleAdapter.ScheduleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemScheduleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(bookings[position])
    }

    override fun getItemCount() = bookings.size

    class ScheduleViewHolder(private val binding: ItemScheduleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Booking) {
            binding.tvScheduleService.text = booking.serviceName
            binding.tvScheduleCustomer.text = booking.customerName
            binding.tvScheduleTime.text = booking.time
            binding.tvScheduleDuration.text = "${booking.durationMin}–${booking.durationMax} min"

            // Color-code status badge
            when (booking.status) {
                "Completed" -> {
                    binding.tvScheduleStatus.text = "Completed"
                    binding.tvScheduleStatus.setTextColor(0xFF1565C0.toInt())
                    binding.tvScheduleStatus.setBackgroundColor(0xFFE3F2FD.toInt())
                }
                "Cancelled" -> {
                    binding.tvScheduleStatus.text = "Cancelled"
                    binding.tvScheduleStatus.setTextColor(0xFFB71C1C.toInt())
                    binding.tvScheduleStatus.setBackgroundColor(0xFFFFEBEE.toInt())
                }
                else -> {
                    binding.tvScheduleStatus.text = "Confirmed"
                    binding.tvScheduleStatus.setTextColor(0xFF2E7D32.toInt())
                    binding.tvScheduleStatus.setBackgroundColor(0xFFE8F5E9.toInt())
                }
            }
        }
    }
}
