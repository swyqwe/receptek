package hu.swy.receptek.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// A receptadatbázis és a hozzá tartozó DAO központi elérési pontja.
@Database(
    entities = [RecipeEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(RecipeConverters::class)
abstract class RecipeDatabase : RoomDatabase() {

    // Hozzáférést ad a receptműveletekhez.
    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var instance: RecipeDatabase? = null

        // Egyetlen közös adatbázispéldányt biztosít az alkalmazásnak.
        fun getInstance(context: Context): RecipeDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    RecipeDatabase::class.java,
                    "recipes.db"
                )
                    .build()
                    .also { database ->
                        instance = database
                    }
            }
        }
    }
}