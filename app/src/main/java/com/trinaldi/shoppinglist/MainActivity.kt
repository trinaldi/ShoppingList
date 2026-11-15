
package com.trinaldi.shoppinglist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trinaldi.shoppinglist.ui.ShoppingListsScreen
import com.trinaldi.shoppinglist.ui.ShoppingScreen
import com.trinaldi.shoppinglist.ui.ShoppingViewModel
import com.trinaldi.shoppinglist.ui.theme.ShoppingListTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ShoppingListTheme {
                val viewModel: ShoppingViewModel = viewModel()
                ShoppingAppRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ShoppingAppRoot(viewModel: ShoppingViewModel) {
    val currentListId by viewModel.currentListId.collectAsState()
    val isCreatingNewList by viewModel.isCreatingNewList.collectAsState()

    val showingShoppingScreen =
        currentListId != null || isCreatingNewList

    BackHandler(enabled = showingShoppingScreen) {
        viewModel.clearSelectedList()
    }

    if (!showingShoppingScreen) {
        ShoppingListsScreen(
            viewModel = viewModel,
            onSelectList = { listId ->
                viewModel.selectList(listId)
            }
        )
    } else {
        ShoppingScreen(
            viewModel = viewModel,
            onBack = {
                viewModel.clearSelectedList()
            }
        )
    }
}
