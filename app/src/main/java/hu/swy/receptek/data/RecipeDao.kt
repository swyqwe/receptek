package hu.swy.receptek.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

// A receptek adatbázis-műveletei.
@Dao
interface RecipeDao {

    // Figyeli a recepteket, és változáskor új listát ad vissza.
    @Query("SELECT * FROM recipes ORDER BY id ASC")
    fun observeAll(): Flow<List<RecipeEntity>>

    // Elment egy új receptet, és visszaadja a generált azonosítóját.
    @Insert
    suspend fun insert(recipe: RecipeEntity): Long

    // Az azonosító alapján módosít egy már létező receptet.
    @Update
    suspend fun update(recipe: RecipeEntity)

    // Az elsődleges kulcs alapján törli a megadott receptet.
    @Delete
    suspend fun delete(recipe: RecipeEntity)

}