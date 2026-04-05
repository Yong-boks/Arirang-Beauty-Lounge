package com.arirang.beautylounge

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arirang.beautylounge.databinding.ItemBookingBinding

class BookingAdapter(private val bookings: List<Booking>) :
    RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(bookings[position])
    }

    override fun getItemCount() = bookings.size

    class BookingViewHolder(private val binding: ItemBookingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Booking) {
            binding.tvBookingService.text = booking.serviceName
            binding.tvBookingCategory.text = booking.serviceCategory
            binding.tvBookingDate.text = booking.date
            binding.tvBookingTime.text = booking.time
            binding.tvBookingStaff.text = booking.staffName
            binding.tvBookingDuration.text = "${booking.durationMin}–${booking.durationMax} min"
            binding.tvBookingPrice.text = "KES ${booking.price}"
            binding.tvBookingStatus.text = booking.status
        }
    }
}
