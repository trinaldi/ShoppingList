package com.trinaldi.shoppinglist.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trinaldi.shoppinglist.data.AppDatabase
import com.trinaldi.shoppinglist.data.ShoppingItem
import com.trinaldi.shoppinglist.data.ShoppingList
import com.trinaldi.shoppinglist.data.ShoppingListWithItems
import com.trinaldi.shoppinglist.data.ShoppingRepository
import com.trinaldi.shoppinglist.data.ShoplistImportData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)

    private val repository = ShoppingRepository(
        database.shoppingItemDao(),
        database.shoppingListDao()
    )

    val allListsWithItems: StateFlow<List<ShoppingListWithItems>> =
        repository.getAllLists().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentListId = MutableStateFlow<Long?>(null)
    val currentListId: StateFlow<Long?> = _currentListId.asStateFlow()

    private val _isCreatingNewList = MutableStateFlow(false)
    val isCreatingNewList: StateFlow<Boolean> = _isCreatingNewList.asStateFlow()

    private val createListMutex = Mutex()

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
        val dateFormat = SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale.getDefault()
        )
        return "Lista de Compras - ${dateFormat.format(Date())}"
    }

    fun selectList(listId: Long) {
        _currentListId.value = listId
        _isCreatingNewList.value = false
    }

    fun clearSelectedList() {
        _currentListId.value = null
        _isCreatingNewList.value = false
    }

    fun createNewList() {
        _currentListId.value = null
        _isCreatingNewList.value = true
    }

    fun addItem(name: String, quantity: String) {
        viewModelScope.launch {
            createListMutex.withLock {
                if (_currentListId.value == null) {
                    if (!_isCreatingNewList.value) {
                        return@withLock
                    }

                    val newId = repository.insertList(
                        ShoppingList(title = generateDefaultTitle())
                    )

                    _currentListId.value = newId
                    _isCreatingNewList.value = false
                }

                val listId = _currentListId.value ?: return@withLock

                val newItem = ShoppingItem(
                    name = name,
                    quantity = quantity,
                    listId = listId,
                    isPurchased = false
                )

                repository.insertItem(newItem)
            }
        }
    }

    fun toggleItemPurchased(item: ShoppingItem) {
        viewModelScope.launch {
            val updatedItem = item.copy(
                isPurchased = !item.isPurchased
            )

            repository.updateItem(updatedItem)

            val updatedItems = repository
                .getItemsForList(item.listId)
                .first()

            val shouldBeFinished = updatedItems.isNotEmpty() &&
                    updatedItems.all { it.isPurchased }

            val currentList = allListsWithItems.value.find {
                it.shoppingList.id == item.listId
            }

            if (currentList != null &&
                currentList.shoppingList.isFinished != shouldBeFinished
            ) {
                repository.updateList(
                    currentList.shoppingList.copy(
                        isFinished = shouldBeFinished
                    )
                )
            }
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

    fun importList(
        data: ShoplistImportData,
        onComplete: (String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val importedItems = data.items.map { item ->
                    ShoppingItem(
                        name = item.name,
                        quantity = item.quantity,
                        isPurchased = item.isPurchased,
                        listId = 0L
                    )
                }

                repository.insertListWithItems(
                    list = ShoppingList(title = data.title),
                    items = importedItems
                )

                onComplete(null)
            } catch (exception: Exception) {
                onComplete(
                    exception.message ?: "Não foi possível importar a lista."
                )
            }
        }
    }
}