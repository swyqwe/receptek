package hu.swy.receptek

// Egy recept alapadatai, kategóriája, hozzávalói és elkészítési lépései.
data class Recipe(
    val id: Long,
    val title: String,
    val description: String,
    val servings: Int,
    val totalMinutes: Int,
    val ingredients: List<String>,
    val steps: List<String>,
    val category: String = "Egyéb"
)