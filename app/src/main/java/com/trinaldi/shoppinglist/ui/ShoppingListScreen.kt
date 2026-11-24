package com.trinaldi.shoppinglist.ui

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.trinaldi.shoppinglist.data.ShoplistFileManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun formatDateTime(timestamp: Long): String {
    val formatter = SimpleDateFormat(
        "dd/MM/yyyy 'às' HH:mm",
        Locale.getDefault()
    )
    return formatter.format(Date(timestamp))
}

private suspend fun shareShoplistFile(
    context: Context,
    content: String,
    title: String
) {
    val safeName = title
        .replace(Regex("[^\\p{L}\\p{N}._-]"), "_")
        .take(50)
        .ifBlank { "lista-de-compras" }

    val file = withContext(Dispatchers.IO) {
        val directory = File(context.cacheDir, "shared")
        directory.mkdirs()

        val outputFile = File(
            directory,
            "$safeName-${System.currentTimeMillis()}.shoplist"
        )

        outputFile.writeText(content, Charsets.UTF_8)
        outputFile
    }

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)

        clipData = ClipData.newUri(
            context.contentResolver,
            file.name,
            uri
        )

        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        Intent.createChooser(
            sendIntent,
            "Compartilhar lista de compras"
        ).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListsScreen(
    viewModel: ShoppingViewModel,
    onSelectList: (Long) -> Unit
) {
    val listsWithItems by viewModel.allListsWithItems.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var dialogMessage by remember {
        mutableStateOf<String?>(null)
    }

    var selectedFinishedList by remember {
        mutableStateOf<Long?>(null)
    }

    var selectedTabIndex by remember {
        mutableIntStateOf(0)
    }

    val activeLists = listsWithItems.filter {
        !it.shoppingList.isFinished
    }

    val finishedLists = listsWithItems.filter {
        it.shoppingList.isFinished
    }

    val visibleLists = if (selectedTabIndex == 0) {
        activeLists
    } else {
        finishedLists
    }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Minhas Listas de Compras",
                            textAlign = TextAlign.Center
                        )
                    }
                )

                SecondaryTabRow(
                    selectedTabIndex = selectedTabIndex
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = {
                            selectedTabIndex = 0
                        },
                        text = {
                            Text("Ativo")
                        }
                    )

                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = {
                            selectedTabIndex = 1
                        },
                        text = {
                            Text("Histórico")
                        }
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        onClick = {
                            viewModel.createNewList()
                        }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Nova Lista",
                                modifier = Modifier.size(28.dp)
                            )

                            Text(
                                text = "Nova Lista",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = visibleLists,
                key = { it.shoppingList.id }
            ) { listWithItems ->

                var showMenu by remember(listWithItems.shoppingList.id) {
                    mutableStateOf(false)
                }

                val isFinished = listWithItems.shoppingList.isFinished

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    if (isFinished) {
                                        selectedFinishedList =
                                            listWithItems.shoppingList.id
                                    } else {
                                        onSelectList(
                                            listWithItems.shoppingList.id
                                        )
                                    }
                                },
                                onLongClick = {
                                    showMenu = true
                                }
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isFinished) {
                                Color(0xFF3B4934)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        if (isFinished) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${listWithItems.shoppingList.title} - Finalizada",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFFE8F5E9)
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = listWithItems.shoppingList.title,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = formatDateTime(
                                        listWithItems.shoppingList.date
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Text(
                                    text = "Itens: ${listWithItems.items.size} | Pendentes: ${
                                        listWithItems.items.count { !it.isPurchased }
                                    }",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Surface(
                                    color = Color(0xFFFFF3E0),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = "Pendente",
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 6.dp
                                        ),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFFE65100)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = {
                                        val message = buildString {
                                            append("*Lista de Compras*")
                                            append("\n\n")
                                            append(
                                                listWithItems.items.joinToString("\n") { item ->
                                                    "- ${item.name} (${item.quantity})"
                                                }
                                            )
                                        }

                                        val sendIntent = Intent(
                                            Intent.ACTION_SEND
                                        ).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                message
                                            )
                                        }

                                        context.startActivity(
                                            Intent.createChooser(
                                                sendIntent,
                                                "Compartilhar lista de compras"
                                            )
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = listWithItems.items.isNotEmpty()
                                ) {
                                    Text("Compartilhar WhatsApp")
                                }

                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            try {
                                                val content =
                                                    ShoplistFileManager.export(
                                                        listWithItems
                                                    )

                                                shareShoplistFile(
                                                    context = context,
                                                    content = content,
                                                    title = listWithItems.shoppingList.title
                                                )
                                            } catch (exception: Exception) {
                                                dialogMessage =
                                                    exception.message
                                                        ?: "Não foi possível compartilhar a lista."
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = listWithItems.items.isNotEmpty()
                                ) {
                                    Text("Exportar e Compartilhar")
                                }
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

    val finishedList = listsWithItems.firstOrNull {
        it.shoppingList.id == selectedFinishedList
    }

    if (finishedList != null) {
        AlertDialog(
            onDismissRequest = {
                selectedFinishedList = null
            },
            title = {
                Text(finishedList.shoppingList.title)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    finishedList.items.forEach { item ->
                        Text(
                            text = "${item.name} - ${item.quantity}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedFinishedList = null
                    }
                ) {
                    Text("Fechar")
                }
            }
        )
    }

    if (dialogMessage != null) {
        AlertDialog(
            onDismissRequest = {
                dialogMessage = null
            },
            title = {
                Text("Lista de Compras")
            },
            text = {
                Text(dialogMessage.orEmpty())
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        dialogMessage = null
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}