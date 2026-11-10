package com.trinaldi.shoppinglist.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trinaldi.shoppinglist.data.AppDatabase
import com.trinaldi.shoppinglist.data.ShoppingItem
import com.trinaldi.shoppinglist.data.ShoppingList
import com.trinaldi.shoppinglist.data.ShoppingListWithItems
import com.trinaldi.shoppinglist.data.ShoppingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = ShoppingRepository(database.shoppingItemDao(), database.shoppingListDao())

    val allListsWithItems: StateFlow<List<ShoppingListWithItems>> = repository.getAllLists()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentListId = MutableStateFlow<Long?>(null)
    val currentListId: StateFlow<Long?> = _currentListId.asStateFlow()

    val items: StateFlow<List<ShoppingItem>> = _currentListId
        .flatMapLatest { listId ->
            if (listId != null) {
                repository.getItemsForList(listId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun generateDefaultTitle(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return "Shopping List - ${dateFormat.format(Date())}"
    }

    fun selectList(listId: Long) {
        _currentListId.value = listId
    }

    fun clearSelectedList() {
        _currentListId.value = null
    }

    fun createNewList() {
        viewModelScope.launch {
            val newId = repository.insertList(ShoppingList(title = generateDefaultTitle()))
            _currentListId.value = newId
        }
    }

    fun addItem(name: String, quantity: Int) {
        val listId = _currentListId.value ?: return
        viewModelScope.launch {
            val newItem = ShoppingItem(
                name = name,
                quantity = quantity,
                listId = listId,
                isPurchased = false
            )
            repository.insertItem(newItem)
        }
    }

    fun toggleItemPurchased(item: ShoppingItem) {
        viewModelScope.launch {
            val updatedItem = item.copy(isPurchased = !item.isPurchased)
            repository.updateItem(updatedItem)
        }
    }

    fun deleteItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun deleteShoppingList(shoppingList: ShoppingList) {
        viewModelScope.launch {
            repository.deleteList(shoppingList)
        }
    }

    fun finishPurchase() {
        viewModelScope.launch {
            val currentId = _currentListId.value ?: return@launch
            val currentListWithItems = allListsWithItems.value.find { it.shoppingList.id == currentId }

            if (currentListWithItems != null) {
                val finishedList = currentListWithItems.shoppingList.copy(isFinished = true)
                repository.updateList(finishedList)
            }

            _currentListId.value = null
        }
    }
}