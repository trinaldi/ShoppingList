package com.trinaldi.shoppinglist.ui

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat(
        "dd/MM/yyyy",
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
    val coroutineScope = rememberCoroutineScope()

    var dialogMessage by remember {
        mutableStateOf<String?>(null)
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val content = withContext(Dispatchers.IO) {
                        val inputStream = context.contentResolver
                            .openInputStream(uri)
                            ?: error("Não foi possível abrir o arquivo.")

                        inputStream.bufferedReader(Charsets.UTF_8).use {
                            it.readText()
                        }
                    }

                    val importedData = ShoplistFileManager.parse(content)

                    viewModel.importList(importedData) { error ->
                        dialogMessage = error
                            ?: "Lista importada com sucesso."
                    }
                } catch (exception: Exception) {
                    dialogMessage = exception.message
                        ?: "Não foi possível importar o arquivo."
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Minhas Listas de Compras")
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        importLauncher.launch(arrayOf("*/*"))
                    }
                ) {
                    Text("Importar lista")
                }

                FloatingActionButton(
                    onClick = {
                        viewModel.createNewList()
                    }
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
                                    text = listWithItems.shoppingList.title,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = formatDateTime(
                                        listWithItems.shoppingList.date
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
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
                                            text = "Finalizada",
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

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Button(
                                    onClick = {
                                        val message = buildString {
                                            append("*Lista de Compras*")
                                            append("\n\n")
                                            append("---")
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
                                    enabled = listWithItems.items.isNotEmpty()
                                ) {
                                    Text("Compartilhar")
                                }

                                Spacer(modifier = Modifier.height(4.dp))

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
                                    enabled = listWithItems.items.isNotEmpty()
                                ) {
                                    Text("Exportar e compartilhar")
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

    if (dialogMessage != null) {
        AlertDialog(
            onDismissRequest = {
                dialogMessage = null
            },
            title = {
                Text("Shopping List")
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