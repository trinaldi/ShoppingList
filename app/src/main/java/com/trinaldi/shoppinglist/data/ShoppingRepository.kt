package com.trinaldi.shoppinglist.data

import kotlinx.coroutines.flow.Flow

class ShoppingRepository(
    private val itemDao: ShoppingItemDao,
    private val listDao: ShoppingListDao
) {
    fun getItemsForList(listId: Long): Flow<List<ShoppingItem>> = itemDao.getItemsForList(listId)

    suspend fun insertItem(item: ShoppingItem) = itemDao.insertItem(item)

    suspend fun updateItem(item: ShoppingItem) = itemDao.updateItem(item)

    suspend fun deleteItem(item: ShoppingItem) = itemDao.deleteItem(item)

    fun getAllLists(): Flow<List<ShoppingListWithItems>> = listDao.getAllListsWithItems()

    suspend fun insertList(list: ShoppingList): Long = listDao.insertList(list)

    suspend fun updateList(list: ShoppingList) = listDao.updateList(list)
    suspend fun deleteList(shoppingList: ShoppingList) {
        listDao.deleteList(shoppingList)
    }
}