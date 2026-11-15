package com.trinaldi.shoppinglist.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import androidx.room3.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Transaction
    @Query("SELECT * FROM shopping_lists ORDER BY date ASC, id ASC")
    fun getAllListsWithItems(): Flow<List<ShoppingListWithItems>>

    @Transaction
    @Query("SELECT * FROM shopping_lists WHERE id = :listId")
    suspend fun getListWithItemsById(listId: Long): ShoppingListWithItems?

    @Insert
    suspend fun insertList(shoppingList: ShoppingList): Long

    @Update
    suspend fun updateList(shoppingList: ShoppingList)

    @Delete
    suspend fun deleteList(shoppingList: ShoppingList)
}
