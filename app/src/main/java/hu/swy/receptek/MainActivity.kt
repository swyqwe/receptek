package hu.swy.receptek

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.swy.receptek.data.RecipeDatabase
import hu.swy.receptek.data.RecipeEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch


// Ideiglenes receptgyűjtemény a felület kipróbálásához.
private val sampleRecipes = listOf(
    Recipe(
        id = 1L,
        title = "Paprikás krumpli",
        description = "Egyszerű, egyedényes vacsora.",
        servings = 4,
        totalMinutes = 45,
        ingredients = listOf(
            "1 kg burgonya",
            "1 fej vöröshagyma",
            "2 evőkanál olaj",
            "1 evőkanál őrölt pirospaprika",
            "Só ízlés szerint",
            "Víz a főzéshez"
        ),
        steps = listOf(
            "Hámozd meg a burgonyát, és vágd darabokra.",
            "Aprítsd fel a hagymát, és párold meg az olajon.",
            "Húzd félre az edényt, és keverd hozzá a pirospaprikát.",
            "Add hozzá a burgonyát, sózd meg, és öntsd fel vízzel.",
            "Főzd addig, amíg a burgonya megpuhul."
        )
    ),
    Recipe(
        id = 2L,
        title = "Palacsinta",
        description = "Alaprecept édes vagy sós töltelékkel.",
        servings = 4,
        totalMinutes = 30,
        ingredients = listOf(
            "250 g liszt",
            "2 db tojás",
            "300 ml tej",
            "200 ml szódavíz",
            "1 csipet só",
            "Olaj a sütéshez"
        ),
        steps = listOf(
            "Keverd össze a lisztet, a tojást, a tejet és a sót.",
            "Fokozatosan keverd hozzá a szódavizet.",
            "Melegíts fel egy enyhén kiolajozott serpenyőt.",
            "Önts bele egy adag tésztát, és oszlasd el.",
            "Süsd meg mindkét oldalát."
        )
    ),
    Recipe(
        id = 3L,
        title = "Paradicsomos tészta",
        description = "Gyors tésztaétel hétköznapokra.",
        servings = 2,
        totalMinutes = 20,
        ingredients = listOf(
            "200 g száraztészta",
            "400 g darabolt paradicsom",
            "2 gerezd fokhagyma",
            "1 evőkanál olívaolaj",
            "Só, bors és bazsalikom ízlés szerint"
        ),
        steps = listOf(
            "Főzd meg a tésztát a csomagolás utasítása szerint.",
            "Aprítsd fel a fokhagymát, és röviden futtasd meg az olajon.",
            "Add hozzá a paradicsomot és a fűszereket.",
            "Főzd össze a szószt.",
            "Keverd össze a leszűrt tésztával."
        )
    )
)

// Az alkalmazás belépési pontja, amely elindítja a Compose felületet.
class MainActivity : ComponentActivity() {

    // Beállítja a képernyő megjelenítését és betölti az alkalmazás felületét.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                RecipeApp()
            }
        }

}




    // A receptlista Android Studio-előnézete mintaadatokkal.
    @Preview(showBackground = true)
    @Composable
    fun RecipeListPreview() {
        MaterialTheme {
            RecipeListScreen(
                recipes = sampleRecipes,
                onRecipeClick = {}
            )
        }
    }

    // Betölti a recepteket, és kezeli a megnyitást, felvitelt és szerkesztést.
    @Composable
    fun RecipeApp() {
        val context = LocalContext.current
        val appContext = context.applicationContext

        val database = remember(appContext) {
            RecipeDatabase.getInstance(appContext)
        }

        val recipeDao = remember(database) {
            database.recipeDao()
        }

        val recipeFlow = remember(recipeDao) {
            recipeDao.observeAll()
        }

        val storedRecipes by recipeFlow.collectAsState(
            initial = emptyList()
        )

        val recipes = storedRecipes.map { entity ->
            entity.toRecipe()
        }

        // Az alaplista mellett a korábbi egyedi kategóriákat is megőrizzük.
        val availableCategories = (
                defaultRecipeCategories +
                        recipes.map { it.category }.sorted()
                ).filter { it.isNotBlank() }
            .distinct()

        val coroutineScope = rememberCoroutineScope()

        var selectedRecipeId by rememberSaveable {
            mutableStateOf<Long?>(null)
        }

        // Megőrzi a keresőszöveget az adatlap megnyitása közben is.
        var searchQuery by rememberSaveable {
            mutableStateOf("")
        }

        // Null esetén minden kategória megjelenik.
        var selectedCategory by rememberSaveable {
            mutableStateOf<String?>(null)
        }

        // Az űrlap új recepthez és szerkesztéshez is megnyitható.
        var isFormOpen by rememberSaveable {
            mutableStateOf(false)
        }

        // Null esetén új receptet veszünk fel.
        var editingRecipeId by rememberSaveable {
            mutableStateOf<Long?>(null)
        }

        var isSaving by remember {
            mutableStateOf(false)
        }

        // Nem null értéknél megjelenítjük a recept törlésének megerősítését.
        var pendingDeleteId by rememberSaveable {
            mutableStateOf<Long?>(null)
        }

        // Megakadályozza ugyanannak a törlésnek a többszöri elindítását.
        var isDeleting by remember {
            mutableStateOf(false)
        }

        val selectedRecipe = recipes.find { recipe ->
            recipe.id == selectedRecipeId
        }

        val editingRecipe = recipes.find { recipe ->
            recipe.id == editingRecipeId
        }

        // A megerősítő ablakban szereplő recept.
        val pendingDeleteRecipe = recipes.find { recipe ->
            recipe.id == pendingDeleteId
        }

        // A tényleges törlés csak az ablakban adott megerősítés után indul.
        if (pendingDeleteRecipe != null) {
            AlertDialog(
                onDismissRequest = {
                    // Folyamatban lévő törlés közben az ablak nem zárható be.
                    if (!isDeleting) {
                        pendingDeleteId = null
                    }
                },
                title = {
                    Text(text = "Recept törlése")
                },
                text = {
                    Text(
                        text = "Biztosan törlöd a(z) " +
                                "„${pendingDeleteRecipe.title}” receptet?\n\n" +
                                "A törlés nem vonható vissza."
                    )
                },
                confirmButton = {
                    TextButton(
                        enabled = !isDeleting,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        onClick = {
                            if (!isDeleting) {
                                isDeleting = true

                                coroutineScope.launch {
                                    try {
                                        val entity = RecipeEntity.fromRecipe(
                                            pendingDeleteRecipe
                                        )

                                        recipeDao.delete(entity)

                                        // Sikeres törlés után bezárjuk az adatlapot.
                                        selectedRecipeId = null
                                        pendingDeleteId = null
                                    } catch (error: CancellationException) {
                                        throw error
                                    } catch (error: Exception) {
                                        Toast.makeText(
                                            appContext,
                                            "Nem sikerült törölni a receptet.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } finally {
                                        isDeleting = false
                                    }
                                }
                            }
                        }
                    ) {
                        Text(
                            text = if (isDeleting) {
                                "Törlés..."
                            } else {
                                "Törlés"
                            }
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        enabled = !isDeleting,
                        onClick = {
                            pendingDeleteId = null
                        }
                    ) {
                        Text(text = "Mégse")
                    }
                }
            )
        }

        when {
            isFormOpen -> {
                BackHandler {
                    if (!isSaving) {
                        isFormOpen = false
                        editingRecipeId = null
                    }
                }

                // Újratöltéskor megvárjuk a szerkesztendő recept adatait.
                if (editingRecipeId != null && editingRecipe == null) {
                    Scaffold { innerPadding ->
                        Text(
                            text = "Recept betöltése...",
                            modifier = Modifier
                                .padding(innerPadding)
                                .padding(16.dp)
                        )
                    }
                } else {
                    RecipeFormScreen(
                        initialRecipe = editingRecipe,
                        availableCategories = availableCategories,
                        onSave = { recipe ->
                            if (!isSaving) {
                                isSaving = true

                                coroutineScope.launch {
                                    try {
                                        val entity =
                                            RecipeEntity.fromRecipe(recipe)

                                        if (recipe.id == 0L) {
                                            // Új recept: az adatbázis ad azonosítót.
                                            recipeDao.insert(entity)
                                        } else {
                                            // Meglévő recept: az azonosító megmarad.
                                            recipeDao.update(entity)
                                        }

                                        isFormOpen = false
                                        editingRecipeId = null
                                    } catch (error: CancellationException) {
                                        throw error
                                    } catch (error: Exception) {
                                        Toast.makeText(
                                            appContext,
                                            "Nem sikerült menteni a receptet.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            }
                        },
                        onCancel = {
                            if (!isSaving) {
                                isFormOpen = false
                                editingRecipeId = null
                            }
                        }
                    )
                }
            }

            selectedRecipe != null -> {
                BackHandler {
                    selectedRecipeId = null
                }

                RecipeDetailScreen(
                    recipe = selectedRecipe,
                    onBack = {
                        selectedRecipeId = null
                    },
                    onEdit = {
                        editingRecipeId = selectedRecipe.id
                        isFormOpen = true
                    },
                    onDelete = {
                        pendingDeleteId = selectedRecipe.id
                    }
                )
            }

            else -> {
                RecipeListScreen(
                    recipes = recipes,
                    onRecipeClick = { recipe ->
                        selectedRecipeId = recipe.id
                    },
                    onAddRecipeClick = {
                        selectedRecipeId = null
                        editingRecipeId = null
                        isFormOpen = true
                    },
                    searchQuery = searchQuery,
                    onSearchQueryChange = { newQuery ->
                        searchQuery = newQuery
                    },
                    selectedCategory = selectedCategory,
                    onCategoryChange = { category ->
                        selectedCategory = category
                    }
                )
            }
        }
    }

    // A receptlista előnézete egy előre megadott kereséssel.
    @Preview(showBackground = true)
    @Composable
    fun RecipeSearchPreview() {
        MaterialTheme {
            RecipeListScreen(
                recipes = sampleRecipes,
                onRecipeClick = {},
                searchQuery = "paradicsom"
            )
        }
    }





}