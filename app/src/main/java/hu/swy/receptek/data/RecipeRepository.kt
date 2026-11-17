package hu.swy.receptek.data

import hu.swy.receptek.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Elrejti az adatbázis részleteit az alkalmazás többi része elől.
class RecipeRepository(
    private val recipeDao: RecipeDao
) {
    // Az adatbázis entitásait a felületen használt receptekké alakítja.
    val recipes: Flow<List<Recipe>> =
        recipeDao.observeAll().map { entities ->
            entities.map { entity ->
                entity.toRecipe()
            }
        }

    // Új receptet beszúr, meglévőt módosít.
    suspend fun save(recipe: Recipe) {
        val entity = RecipeEntity.fromRecipe(recipe)

        if (recipe.id == 0L) {
            recipeDao.insert(entity)
        } else {
            recipeDao.update(entity)
        }
    }

    // Eltávolítja a megadott receptet.
    suspend fun delete(recipe: Recipe) {
        recipeDao.delete(
            RecipeEntity.fromRecipe(recipe)
        )
    }
}