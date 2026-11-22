package com.trinaldi.shoppinglist

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trinaldi.shoppinglist.data.ShoplistFileManager
import com.trinaldi.shoppinglist.data.ShoplistImportData
import com.trinaldi.shoppinglist.ui.ShoppingListsScreen
import com.trinaldi.shoppinglist.ui.ShoppingScreen
import com.trinaldi.shoppinglist.ui.ShoppingViewModel
import com.trinaldi.shoppinglist.ui.theme.ShoppingListTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val incomingShoplistUri = mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        incomingShoplistUri.value = getShoplistUri(intent)

        setContent {
            ShoppingListTheme {
                val viewModel: ShoppingViewModel = viewModel()

                ShoppingAppRoot(
                    viewModel = viewModel,
                    incomingShoplistUri = incomingShoplistUri.value,
                    onImportHandled = {
                        incomingShoplistUri.value = null
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incomingShoplistUri.value = getShoplistUri(intent)
    }

    private fun getShoplistUri(intent: Intent?): Uri? {
        if (intent == null) return null

        return intent.data
            ?: intent.clipData?.getItemAt(0)?.uri
            ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(
                    Intent.EXTRA_STREAM,
                    Uri::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
    }
}

@Composable
fun ShoppingAppRoot(
    viewModel: ShoppingViewModel,
    incomingShoplistUri: Uri?,
    onImportHandled: () -> Unit
) {
    val currentListId by viewModel.currentListId.collectAsState()
    val isCreatingNewList by viewModel.isCreatingNewList.collectAsState()

    val context = LocalContext.current

    var pendingImport by remember {
        mutableStateOf<ShoplistImportData?>(null)
    }

    var importMessage by remember {
        mutableStateOf<String?>(null)
    }

    var isImporting by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(incomingShoplistUri) {
        val uri = incomingShoplistUri ?: return@LaunchedEffect

        try {
            val content = withContext(Dispatchers.IO) {
                val inputStream = context.contentResolver
                    .openInputStream(uri)
                    ?: error("Não foi possível abrir o arquivo.")

                inputStream.bufferedReader(Charsets.UTF_8).use {
                    it.readText()
                }
            }

            pendingImport = ShoplistFileManager.parse(content)
        } catch (exception: Exception) {
            importMessage = exception.message
                ?: "Não foi possível abrir o arquivo."
        } finally {
            onImportHandled()
        }
    }

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

    pendingImport?.let { data ->
        AlertDialog(
            onDismissRequest = {
                pendingImport = null
            },
            title = {
                Text("Importar lista?")
            },
            text = {
                Text(
                    "Deseja importar a lista \"${data.title}\" " +
                            "com ${data.items.size} itens?"
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !isImporting,
                    onClick = {
                        isImporting = true

                        viewModel.importList(data) { error ->
                            isImporting = false
                            pendingImport = null

                            importMessage = error
                                ?: "Lista importada com sucesso."
                        }
                    }
                ) {
                    Text("Importar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingImport = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    importMessage?.let { message ->
        AlertDialog(
            onDismissRequest = {
                importMessage = null
            },
            title = {
                Text("Shopping List")
            },
            text = {
                Text(message)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        importMessage = null
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}