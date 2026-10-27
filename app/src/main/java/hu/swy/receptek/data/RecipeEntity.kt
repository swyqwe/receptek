package hu.swy.receptek.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import hu.swy.receptek.Recipe

// A recipes tábla egy sorát leíró adatbázis-entitás.
@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val description: String,
    val servings: Int,
    val totalMinutes: Int,
    val ingredients: List<String>,
    val steps: List<String>
) {
    // Az adatbázisban tárolt recept átalakítása a felület modelljévé.
    fun toRecipe(): Recipe {
        return Recipe(
            id = id,
            title = title,
            description = description,
            servings = servings,
            totalMinutes = totalMinutes,
            ingredients = ingredients,
            steps = steps
        )
    }

    companion object {
        // A felületen használt recept átalakítása adatbázis-entitássá.
        fun fromRecipe(recipe: Recipe): RecipeEntity {
            return RecipeEntity(
                id = recipe.id,
                title = recipe.title,
                description = recipe.description,
                servings = recipe.servings,
                totalMinutes = recipe.totalMinutes,
                ingredients = recipe.ingredients,
                steps = recipe.steps
            )
        }
    }
}