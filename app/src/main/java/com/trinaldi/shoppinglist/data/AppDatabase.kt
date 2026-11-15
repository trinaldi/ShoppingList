
package com.trinaldi.shoppinglist.data

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

@Database(
    entities = [ShoppingItem::class, ShoppingList::class],
    version = 4
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun shoppingItemDao(): ShoppingItemDao
    abstract fun shoppingListDao(): ShoppingListDao

    companion object {

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override suspend fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                    ALTER TABLE shopping_items
                    ADD COLUMN quantity_new TEXT NOT NULL DEFAULT '1'
                    """.trimIndent()
                )

                connection.execSQL(
                    """
                    UPDATE shopping_items
                    SET quantity_new = CAST(quantity AS TEXT)
                    """.trimIndent()
                )

                connection.execSQL(
                    """
                    ALTER TABLE shopping_items DROP COLUMN quantity
                    """.trimIndent()
                )

                connection.execSQL(
                    """
                    ALTER TABLE shopping_items
                    RENAME COLUMN quantity_new TO quantity
                    """.trimIndent()
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shopping_database"
                )
                    .addMigrations(MIGRATION_3_4)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
