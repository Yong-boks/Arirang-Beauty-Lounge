package com.arirang.beautylounge

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arirang.beautylounge.databinding.ItemCustomerBinding

class StaffCustomerAdapter(private val customers: List<CustomerSummary>) :
    RecyclerView.Adapter<StaffCustomerAdapter.CustomerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val binding = ItemCustomerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CustomerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        holder.bind(customers[position])
    }

    override fun getItemCount() = customers.size

    class CustomerViewHolder(private val binding: ItemCustomerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(customer: CustomerSummary) {
            binding.tvCustomerName.text = customer.customerName
            val bookingText = if (customer.bookingCount == 1) "1 booking" else "${customer.bookingCount} bookings"
            binding.tvCustomerBookingCount.text = bookingText
            binding.tvLastService.text = customer.lastService.ifEmpty { "—" }
            binding.tvLastDate.text = customer.lastDate.ifEmpty { "—" }
        }
    }
}
