package com.trinaldi.shoppinglist.data

import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import androidx.room3.Dao
import kotlinx.coroutines.flow.Flow


@Dao
interface ShoppingItemDao {
    @Query("SELECT * FROM shopping_items WHERE listId = :listId")
    fun getItemsForList(listId: Long): Flow<List<ShoppingItem>>
    @Insert
    suspend fun insertItem(item: ShoppingItem)
    @Update
    suspend fun updateItem(item: ShoppingItem)
    @Delete
    suspend fun deleteItem(item: ShoppingItem)
}