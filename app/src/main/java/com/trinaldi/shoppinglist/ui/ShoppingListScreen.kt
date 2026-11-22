
package com.trinaldi.shoppinglist.ui

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDateTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun ShoppingListsScreen(
    viewModel: ShoppingViewModel,
    onSelectList: (Long) -> Unit
) {
    val listsWithItems by viewModel.allListsWithItems.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Minhas Listas de Compras")
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.createNewList()
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Nova Lista de Compras",
                    modifier = Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    ),
                    softWrap = true
                )
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

                var showMenu by remember(listWithItems.shoppingList.id) {
                    mutableStateOf(false)
                }

                val hasPendingItems =
                    listWithItems.items.any { !it.isPurchased }

                val isActuallyFinished =
                    listWithItems.shoppingList.isFinished && !hasPendingItems

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    onSelectList(listWithItems.shoppingList.id)
                                },
                                onLongClick = {
                                    showMenu = true
                                }
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActuallyFinished) {
                                Color(0xFF3B4934)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Lista de Compras - ${
                                        formatDateTime(listWithItems.shoppingList.date)
                                    }",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Itens: ${listWithItems.items.size} | Pendentes: ${
                                        listWithItems.items.count { !it.isPurchased }
                                    }",
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
                                            modifier = Modifier.padding(
                                                horizontal = 8.dp,
                                                vertical = 4.dp
                                            ),
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
                                            modifier = Modifier.padding(
                                                horizontal = 8.dp,
                                                vertical = 4.dp
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFE65100)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    val message = buildString {
                                        append("*Lista de Compras*")
                                        append("---")
                                        append("\n\n")
                                        append(
                                            listWithItems.items.joinToString("\n") { item ->
                                                "- ${item.name} (${item.quantity})"
                                            }
                                        )
                                    }

                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, message)
                                    }

                                    context.startActivity(
                                        Intent.createChooser(
                                            sendIntent,
                                            "Compartilhar lista de compras"
                                        )
                                    )
                                },
                                enabled = listWithItems.items.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text("Compartilhar")
                            }
                        }
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = {
                            showMenu = false
                        }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Excluir",
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showMenu = false
                                viewModel.deleteShoppingList(
                                    listWithItems.shoppingList
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
