package com.trinaldi.shoppinglist.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "shopping_lists")
data class ShoppingList (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val date: Long = System.currentTimeMillis(),
    val isFinished: Boolean = false
 )