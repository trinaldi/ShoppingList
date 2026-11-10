package com.trinaldi.shoppinglist.data

import androidx.room3.Embedded
import androidx.room3.Relation

data class ShoppingListWithItems(
    @Embedded
    val shoppingList: ShoppingList,

    @Relation(
        parentColumns = ["id"],
        entityColumns = ["listId"]
    )
    val items: List<ShoppingItem>
)