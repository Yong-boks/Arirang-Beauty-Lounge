package com.arirang.beautylounge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arirang.beautylounge.databinding.ItemBookingBinding

class BookingAdapter(
    private val bookings: List<Booking>,
    private val onCancelClick: ((Booking) -> Unit)? = null
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(bookings[position], onCancelClick)
    }

    override fun getItemCount() = bookings.size

    class BookingViewHolder(private val binding: ItemBookingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Booking, onCancelClick: ((Booking) -> Unit)?) {
            binding.tvBookingService.text = booking.serviceName
            binding.tvBookingCategory.text = booking.serviceCategory
            binding.tvBookingDate.text = booking.date
            binding.tvBookingTime.text = booking.time
            binding.tvBookingStaff.text = booking.staffName
            binding.tvBookingDuration.text = "${booking.durationMin}–${booking.durationMax} min"
            binding.tvBookingPrice.text = "KES ${booking.price}"
            binding.tvBookingStatus.text = booking.status

            // Color-code status badge
            when (booking.status) {
                "Completed" -> {
                    binding.tvBookingStatus.setTextColor(0xFF1565C0.toInt())
                    binding.tvBookingStatus.setBackgroundColor(0xFFE3F2FD.toInt())
                }
                "Cancelled" -> {
                    binding.tvBookingStatus.setTextColor(0xFFB71C1C.toInt())
                    binding.tvBookingStatus.setBackgroundColor(0xFFFFEBEE.toInt())
                }
                else -> {
                    binding.tvBookingStatus.setTextColor(0xFF2E7D32.toInt())
                    binding.tvBookingStatus.setBackgroundColor(0xFFE8F5E9.toInt())
                }
            }

            // Show cancel button only for confirmed bookings
            if (onCancelClick != null && booking.status == "Confirmed") {
                binding.btnCancelBooking.visibility = View.VISIBLE
                binding.btnCancelBooking.setOnClickListener { onCancelClick(booking) }
            } else {
                binding.btnCancelBooking.visibility = View.GONE
            }
        }
    }
}
