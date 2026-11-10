package com.trinaldi.shoppinglist.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDateTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListsScreen(
    viewModel: ShoppingViewModel,
    onSelectList: (Long) -> Unit
) {
    val listsWithItems by viewModel.allListsWithItems.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Minhas Listas de Compras") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.createNewList()
            }) {
                Text("+")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listsWithItems) { listWithItems ->
                val hasPendingItems = listWithItems.items.any { !it.isPurchased }
                val isActuallyFinished = listWithItems.shoppingList.isFinished && !hasPendingItems

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectList(listWithItems.shoppingList.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Shopping List - ${formatDateTime(listWithItems.shoppingList.date)}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Items: ${listWithItems.items.size} | Pending: ${listWithItems.items.count { !it.isPurchased }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (isActuallyFinished) {
                                Surface(
                                    color = Color(0xFFE8F5E9),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = "Finalizada ✓",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            } else {
                                Surface(
                                    color = Color(0xFFFFF3E0),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = "Pendente",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFE65100)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.deleteShoppingList(listWithItems.shoppingList) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}
