package hu.swy.receptek.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// A receptadatbázis és a hozzá tartozó DAO központi elérési pontja.
@Database(
    entities = [RecipeEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(RecipeConverters::class)
abstract class RecipeDatabase : RoomDatabase() {

    // Hozzáférést ad a receptműveletekhez.
    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var instance: RecipeDatabase? = null

        // Hozzáadja a kategóriát a meglévő receptek törlése nélkül.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE recipes
                    ADD COLUMN category TEXT NOT NULL DEFAULT 'Egyéb'
                    """.trimIndent()
                )
            }
        }

        // Egyetlen közös adatbázispéldányt biztosít az alkalmazásnak.
        fun getInstance(context: Context): RecipeDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    RecipeDatabase::class.java,
                    "recipes.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { database ->
                        instance = database
                    }
            }
        }
    }
}