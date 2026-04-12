package com.arirang.beautylounge

data class InventoryItem(
    val id: String = "",
    val name: String = "",
    val quantity: Int = 0,
    val unit: String = "",
    val lowStockThreshold: Int = 5,
    val updatedAt: Long = 0L
) {
    val isLowStock: Boolean get() = quantity <= lowStockThreshold
}
