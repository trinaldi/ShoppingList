package com.trinaldi.shoppinglist.data

import org.json.JSONArray
import org.json.JSONObject

data class ShoplistImportData(
    val title: String,
    val items: List<ShoplistImportItem>
)

data class ShoplistImportItem(
    val name: String,
    val quantity: String,
    val isPurchased: Boolean
)

object ShoplistFileManager {

    private const val FORMAT = "com.trinaldi.shoppinglist"
    private const val VERSION = 1

    fun export(list: ShoppingListWithItems): String {
        val itemsJson = JSONArray()

        list.items.forEach { item ->
            val itemJson = JSONObject().apply {
                put("name", item.name)
                put("quantity", item.quantity)
                put("isPurchased", item.isPurchased)
            }

            itemsJson.put(itemJson)
        }

        val listJson = JSONObject().apply {
            put("title", list.shoppingList.title)
            put("items", itemsJson)
        }

        return JSONObject().apply {
            put("format", FORMAT)
            put("version", VERSION)
            put("list", listJson)
        }.toString(2)
    }

    fun parse(content: String): ShoplistImportData {
        require(content.toByteArray(Charsets.UTF_8).size <= 1_048_576) {
            "O arquivo excede o tamanho máximo permitido."
        }

        val root = JSONObject(content)

        require(root.opt("format") == FORMAT) {
            "Este arquivo não é um arquivo ShoppingList válido."
        }

        val version = root.opt("version") as? Number

        require(version?.toInt() == VERSION) {
            "A versão deste arquivo não é compatível."
        }

        val listJson = root.optJSONObject("list")
            ?: throw IllegalArgumentException("A lista não foi encontrada no arquivo.")

        val title = listJson.opt("title") as? String

        require(!title.isNullOrBlank()) {
            "O título da lista é inválido."
        }

        val itemsJson = listJson.optJSONArray("items")
            ?: throw IllegalArgumentException("Os itens da lista não foram encontrados.")

        val items = mutableListOf<ShoplistImportItem>()

        for (index in 0 until itemsJson.length()) {
            val itemJson = itemsJson.optJSONObject(index)
                ?: throw IllegalArgumentException("Um dos itens do arquivo é inválido.")

            val name = itemJson.opt("name") as? String
            val quantity = itemJson.opt("quantity") as? String
            val isPurchased = itemJson.opt("isPurchased") as? Boolean

            require(!name.isNullOrBlank()) {
                "O nome de um dos itens é inválido."
            }

            require(!quantity.isNullOrBlank()) {
                "A quantidade de um dos itens é inválida."
            }

            require(isPurchased != null) {
                "O estado de compra de um dos itens é inválido."
            }

            items.add(
                ShoplistImportItem(
                    name = name,
                    quantity = quantity,
                    isPurchased = isPurchased
                )
            )
        }

        return ShoplistImportData(
            title = title,
            items = items
        )
    }
}