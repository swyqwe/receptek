package hu.swy.receptek

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.swy.receptek.data.RecipeDatabase
import hu.swy.receptek.data.RecipeEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

// Egy recept alapadatai, hozzávalói és elkészítési lépései.
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

// Az új receptekhez alapértelmezetten választható kategóriák.
private val defaultRecipeCategories = listOf(
    "Egyéb",
    "Előétel",
    "Leves",
    "Főétel",
    "Köret",
    "Saláta",
    "Desszert",
    "Péksütemény",
    "Ital"
)

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


    // Megjeleníti a recepteket, és kezeli a megnyitás vagy új felvitel jelzését.
    // Kereshető receptlista, találatszámmal és üresállapot-jelzéssel.
    // Kereshető és kategória szerint szűrhető receptlista.
    @Composable
    fun RecipeListScreen(
        recipes: List<Recipe>,
        onRecipeClick: (Recipe) -> Unit,
        modifier: Modifier = Modifier,
        onAddRecipeClick: () -> Unit = {},
        searchQuery: String = "",
        onSearchQueryChange: (String) -> Unit = {},
        selectedCategory: String? = null,
        onCategoryChange: (String?) -> Unit = {}
    ) {
        // A szélső szóközöket nem vesszük figyelembe a keresésnél.
        val query = searchQuery.trim()

// A szűrőben csak ténylegesen használt kategóriák jelennek meg.
        val categories = recipes
            .map { it.category }
            .distinct()
            .sorted()

// Ha egy kategória megszűnt, automatikusan az összes receptet mutatjuk.
        val activeCategory = selectedCategory?.takeIf {
            it in categories
        }

// A keresésnek és a kategóriaszűrésnek egyszerre kell teljesülnie.
        val filteredRecipes = recipes.filter { recipe ->
            val matchesSearch =
                query.isEmpty() ||
                        recipe.title.contains(query, ignoreCase = true) ||
                        recipe.ingredients.any { ingredient ->
                            ingredient.contains(query, ignoreCase = true)
                        }

            val matchesCategory =
                activeCategory == null || recipe.category == activeCategory

            matchesSearch && matchesCategory
        }

        Scaffold(
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text(
                    text = "Recepttár",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(16.dp)
                )

                Button(
                    onClick = onAddRecipeClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 12.dp
                        )
                ) {
                    Text(text = "Új recept")
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = {
                        Text(text = "Keresés")
                    },
                    placeholder = {
                        Text(text = "Recept neve vagy hozzávaló")
                    },
                    singleLine = true,
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    onSearchQueryChange("")
                                }
                            ) {
                                Text(text = "Törlés")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                // A kategóriák sora oldalirányban görgethető.
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = activeCategory == null,
                            onClick = {
                                onCategoryChange(null)
                            },
                            label = {
                                Text(text = "Összes")
                            }
                        )
                    }

                    items(
                        items = categories,
                        key = { it }
                    ) { categoryName ->
                        FilterChip(
                            selected = activeCategory == categoryName,
                            onClick = {
                                onCategoryChange(categoryName)
                            },
                            label = {
                                Text(text = categoryName)
                            }
                        )
                    }
                }

                Text(
                    text = if (query.isEmpty() && activeCategory == null) {
                        "Receptek: ${recipes.size}"
                    } else {
                        "Találatok: ${filteredRecipes.size} / ${recipes.size}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    )
                )

                when {
                    recipes.isEmpty() -> {
                        Text(
                            text = "Még nincs mentett recepted. " +
                                    "Az „Új recept” gombbal veheted fel az elsőt.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    filteredRecipes.isEmpty() -> {
                        Text(
                            text = "Nincs a keresésnek és a kategóriaszűrésnek megfelelő recept.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = filteredRecipes,
                                key = { recipe -> recipe.id }
                            ) { recipe ->
                                RecipeCard(
                                    recipe = recipe,
                                    onClick = {
                                        onRecipeClick(recipe)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Egy recept rövid összefoglalója kattintható kártyán.
    @Composable
    fun RecipeCard(
        recipe: Recipe,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = recipe.category,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = recipe.description,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "${recipe.servings} adag • ${recipe.totalMinutes} perc",
                    style = MaterialTheme.typography.labelLarge
                )
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

    // Új recept felvitele vagy egy meglévő recept adatainak szerkesztése.
    @Composable
    fun RecipeFormScreen(
        onSave: (Recipe) -> Unit,
        onCancel: () -> Unit,
        modifier: Modifier = Modifier,
        initialRecipe: Recipe? = null,
        availableCategories: List<String> = defaultRecipeCategories
    ) {
        var title by rememberSaveable(initialRecipe?.id) {
            mutableStateOf(initialRecipe?.title ?: "")
        }

        var description by rememberSaveable(initialRecipe?.id) {
            mutableStateOf(initialRecipe?.description ?: "")
        }

        var servingsText by rememberSaveable(initialRecipe?.id) {
            mutableStateOf(initialRecipe?.servings?.toString() ?: "2")
        }

        var minutesText by rememberSaveable(initialRecipe?.id) {
            mutableStateOf(initialRecipe?.totalMinutes?.toString() ?: "30")
        }

        var ingredientsText by rememberSaveable(initialRecipe?.id) {
            mutableStateOf(initialRecipe?.ingredients?.joinToString("\n") ?: "")
        }

        var stepsText by rememberSaveable(initialRecipe?.id) {
            mutableStateOf(initialRecipe?.steps?.joinToString("\n") ?: "")
        }

        // Szerkesztéskor a korábbi kategóriával töltjük ki a mezőt.
        var category by rememberSaveable(initialRecipe?.id) {
            mutableStateOf(initialRecipe?.category ?: "Egyéb")
        }

        // A kiválasztott érték akkor is választható marad, ha egy régi egyedi kategória.
        val categoryOptions = (
                availableCategories + category
                ).filter { it.isNotBlank() }
            .distinct()

        // A hibás vagy túl nagy szám nem okoz kivételt, hanem null értéket ad.
        val servings = servingsText.toIntOrNull()
        val totalMinutes = minutesText.toIntOrNull()

        // Egy nem üres sor egy hozzávalót, illetve egy elkészítési lépést jelent.
        val ingredients = ingredientsText.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val steps = stepsText.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val isServingsValid = servings != null && servings > 0
        val isTimeValid = totalMinutes != null && totalMinutes > 0

        val canSave = title.isNotBlank() &&
                isServingsValid &&
                isTimeValid &&
                ingredients.isNotEmpty() &&
                steps.isNotEmpty()

        Scaffold(
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (initialRecipe == null) {
                        "Új recept"
                    } else {
                        "Recept szerkesztése"
                    },
                    style = MaterialTheme.typography.headlineLarge
                )

                Text(
                    text = "A leírás kivételével minden mező kitöltése kötelező.",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Recept neve") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Rövid leírás") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                CategoryDropdown(
                    selectedCategory = category,
                    categories = categoryOptions,
                    onCategorySelected = { selected ->
                        category = selected
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = servingsText,
                    onValueChange = { servingsText = it },
                    label = { Text("Adagszám") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    isError = !isServingsValid,
                    supportingText = {
                        if (!isServingsValid) {
                            Text("Adj meg egy nullánál nagyobb egész számot.")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = minutesText,
                    onValueChange = { minutesText = it },
                    label = { Text("Teljes időigény percben") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    isError = !isTimeValid,
                    supportingText = {
                        if (!isTimeValid) {
                            Text("Adj meg egy nullánál nagyobb egész számot.")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ingredientsText,
                    onValueChange = { ingredientsText = it },
                    label = { Text("Hozzávalók") },
                    placeholder = {
                        Text("200 g tészta\n400 g paradicsom\n1 csipet só")
                    },
                    supportingText = {
                        Text("Minden hozzávalót külön sorba írj.")
                    },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = stepsText,
                    onValueChange = { stepsText = it },
                    label = { Text("Elkészítési lépések") },
                    placeholder = {
                        Text("Készítsd elő a hozzávalókat.\nFőzd meg a tésztát.")
                    },
                    supportingText = {
                        Text("Minden lépést külön sorba írj, számozás nélkül.")
                    },
                    minLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        // Mentéskor is ellenőrizzük a számmá alakítást.
                        val validServings =
                            servingsText.toIntOrNull() ?: return@Button
                        val validMinutes =
                            minutesText.toIntOrNull() ?: return@Button

                        if (canSave) {
                            onSave(
                                Recipe(
                                    id = initialRecipe?.id ?: 0L,
                                    title = title.trim(),
                                    description = description.trim(),
                                    servings = validServings,
                                    totalMinutes = validMinutes,
                                    ingredients = ingredients,
                                    steps = steps,
                                    category = category.trim().ifBlank { "Egyéb" }
                                )
                            )
                        }
                    },
                    enabled = canSave,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Recept mentése")
                }

                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Mégse")
                }
            }
        }
    }

    // Görgethető adatlapon mutatja a receptet, a hozzávalókat és a lépéseket.
    // Megjeleníti a recept részleteit, és lehetőséget ad a szerkesztésre.
    @Composable
    fun RecipeDetailScreen(
        recipe: Recipe,
        onBack: () -> Unit,
        modifier: Modifier = Modifier,
        onEdit: () -> Unit = {},
        onDelete: () -> Unit = {}
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onBack
                ) {
                    Text(text = "Vissza a receptekhez")
                }
                // A recept adataival kitöltött űrlap megnyitása.
                Button(
                    onClick = onEdit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Szerkesztés")
                }
                // A gomb csak a törlés megerősítését kéri, közvetlenül nem töröl.
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(text = "Recept törlése")
                }
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.headlineLarge
                )

                Text(
                    text = "Kategória: ${recipe.category}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = recipe.description,
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = "${recipe.servings} adag • ${recipe.totalMinutes} perc",
                    style = MaterialTheme.typography.labelLarge
                )

                Text(
                    text = "Hozzávalók",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 12.dp)
                )

                recipe.ingredients.forEach { ingredient ->
                    Text(
                        text = "• $ingredient",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Text(
                    text = "Elkészítés",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 12.dp)
                )

                recipe.steps.forEachIndexed { index, step ->
                    Text(
                        text = "${index + 1}. $step",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }

    // Egy kategória kiválasztása a lenyitható listából.
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CategoryDropdown(
        selectedCategory: String,
        categories: List<String>,
        onCategorySelected: (String) -> Unit,
        modifier: Modifier = Modifier
    ) {
        var expanded by remember {
            mutableStateOf(false)
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = modifier
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                label = {
                    Text(text = "Kategória")
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(
                        ExposedDropdownMenuAnchorType.PrimaryNotEditable
                    )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {
                categories.forEach { categoryName ->
                    DropdownMenuItem(
                        text = {
                            Text(text = categoryName)
                        },
                        onClick = {
                            onCategorySelected(categoryName)
                            expanded = false
                        },
                        contentPadding =
                            ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
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