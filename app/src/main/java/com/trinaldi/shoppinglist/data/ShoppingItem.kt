package com.trinaldi.shoppinglist.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val quantity: String,
    val isPurchased: Boolean = false,
    var listId: Long
)
