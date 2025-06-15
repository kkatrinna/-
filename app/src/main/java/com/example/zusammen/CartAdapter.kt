object Cart {
    private val items = mutableListOf<CartItem>()

    data class CartItem(
        val name: String,
        val description: String,
        val price: String,
        val imageRes: Int,
        val category: String
    )

    fun addItem(item: CartItem) {
        items.add(item)
    }

    fun removeItem(item: CartItem) {
        items.remove(item)
    }

    fun getItems(): List<CartItem> = items.toList()

    fun clearCart() {
        items.clear()
    }

    fun calculateTotal(): String {
        var total = 0
        items.forEach { item ->
            val priceValue = item.price.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0
            total += priceValue
        }
        return "$total ₽"
    }
}