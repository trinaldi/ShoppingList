
package com.trinaldi.shoppinglist.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart

private val PurchasedBackground = Color(0xFF2E7D32)
private val PurchasedStatusBackground = Color(0xFF1B5E20)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(
    viewModel: ShoppingViewModel,
    onBack: () -> Unit
) {
    val items by viewModel.items.collectAsState()

    var itemName by remember { mutableStateOf("") }
    var itemQuantity by remember { mutableStateOf("") }
    var showAddItemDialog by remember { mutableStateOf(false) }

    val nameFocusRequester = remember { FocusRequester() }
    val quantityFocusRequester = remember { FocusRequester() }

    val addItem = {
        if (itemName.isNotBlank()) {
            viewModel.addItem(
                name = itemName.trim(),
                quantity = itemQuantity.trim().ifBlank { "1" }
            )

            itemName = ""
            itemQuantity = ""
            showAddItemDialog = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Itens para comprar") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                tonalElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Button(
                        onClick = { viewModel.finishPurchase() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(
                            text = "FINALIZAR COMPRA",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    itemName = ""
                    itemQuantity = ""
                    showAddItemDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "ADICIONAR ITEM",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { item ->
                    val textColor = if (item.isPurchased) {
                        Color.White
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isPurchased) {
                                PurchasedBackground
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 3.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )

                                    Text(
                                        text = "Quantidade: ${item.quantity}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        viewModel.deleteItem(item)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Excluir item",
                                        tint = if (item.isPurchased) {
                                            Color.White
                                        } else {
                                            MaterialTheme.colorScheme.error
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (item.isPurchased) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = PurchasedStatusBackground,
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(30.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "JÁ COMPRADO",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    viewModel.toggleItemPurchased(item)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (item.isPurchased) {
                                        Color.White
                                    } else {
                                        Color(0xFF176B70)
                                    },
                                    contentColor = if (item.isPurchased) {
                                        PurchasedStatusBackground
                                    } else {
                                        Color.White
                                    }
                                )
                            ) {
                                Icon(
                                    imageVector = if (item.isPurchased) {
                                        Icons.Default.ShoppingCart
                                    } else {
                                        Icons.Default.CheckCircle
                                    },
                                    contentDescription = null
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = if (item.isPurchased) {
                                        "DESFAZER"
                                    } else {
                                        "MARCAR COMO COMPRADO"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddItemDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddItemDialog = false
            },
            title = {
                Text(
                    text = "Adicionar item",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(nameFocusRequester),
                        label = { Text("Nome do item") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                quantityFocusRequester.requestFocus()
                            }
                        )
                    )

                    OutlinedTextField(
                        value = itemQuantity,
                        onValueChange = { itemQuantity = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(quantityFocusRequester),
                        label = { Text("Quantidade") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                addItem()
                            }
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        addItem()
                    },
                    enabled = itemName.isNotBlank(),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("ADICIONAR")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddItemDialog = false
                    },
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("CANCELAR")
                }
            }
        )
    }
}