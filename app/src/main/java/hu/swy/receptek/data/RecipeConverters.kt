package hu.swy.receptek.data

import androidx.room.TypeConverter
import org.json.JSONArray

// A szöveges listák és az adatbázisban tárolható JSON közötti átalakítás.
class RecipeConverters {

    // A lista elemeit egyetlen JSON-szöveggé alakítja.
    @TypeConverter
    fun fromStringList(values: List<String>): String {
        return JSONArray(values).toString()
    }

    // A tárolt JSON-szövegből visszaállítja a lista elemeit.
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val jsonArray = JSONArray(value)

        return List(jsonArray.length()) { index ->
            jsonArray.getString(index)
        }
    }
}