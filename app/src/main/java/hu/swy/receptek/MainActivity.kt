package hu.swy.receptek

import android.os.Bundle
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Egy recept alapadatai, hozzávalói és elkészítési lépései.
data class Recipe(
    val id: Long,
    val title: String,
    val description: String,
    val servings: Int,
    val totalMinutes: Int,
    val ingredients: List<String>,
    val steps: List<String>
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
    @Composable
    fun RecipeListScreen(
        recipes: List<Recipe>,
        onRecipeClick: (Recipe) -> Unit,
        modifier: Modifier = Modifier,
        onAddRecipeClick: () -> Unit = {}
    ) {
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
                        items = recipes,
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

    // Kezeli a receptgyűjteményt és a lista, adatlap, illetve űrlap közötti váltást.
    @Composable
    fun RecipeApp() {
        // Átmeneti gyűjtemény: az új receptek még nem kerülnek adatbázisba.
        val recipes = remember {
            mutableStateListOf<Recipe>().apply {
                addAll(sampleRecipes)
            }
        }

        var selectedRecipeId by rememberSaveable {
            mutableStateOf<Long?>(null)
        }

        var isAddingRecipe by rememberSaveable {
            mutableStateOf(false)
        }

        val selectedRecipe = recipes.find { recipe ->
            recipe.id == selectedRecipeId
        }

        when {
            isAddingRecipe -> {
                BackHandler {
                    isAddingRecipe = false
                }

                RecipeFormScreen(
                    onSave = { newRecipe ->
                        // A következő szabad azonosítót a gyűjteményből számítjuk.
                        val nextId = (recipes.maxOfOrNull { it.id } ?: 0L) + 1L

                        recipes.add(
                            newRecipe.copy(id = nextId)
                        )

                        isAddingRecipe = false
                    },
                    onCancel = {
                        isAddingRecipe = false
                    }
                )
            }

            selectedRecipe != null -> {
                BackHandler {
                    selectedRecipeId = null
                }

                RecipeDetailScreen(
                    recipe = selectedRecipe,
                    onBack = {
                        selectedRecipeId = null
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
                        isAddingRecipe = true
                    }
                )
            }
        }
    }


    // Új recept adatainak megadása, ellenőrzése és átadása mentésre.
    @Composable
    fun RecipeFormScreen(
        onSave: (Recipe) -> Unit,
        onCancel: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        var title by rememberSaveable { mutableStateOf("") }
        var description by rememberSaveable { mutableStateOf("") }
        var servingsText by rememberSaveable { mutableStateOf("2") }
        var minutesText by rememberSaveable { mutableStateOf("30") }
        var ingredientsText by rememberSaveable { mutableStateOf("") }
        var stepsText by rememberSaveable { mutableStateOf("") }

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
                    text = "Új recept",
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
                                    id = 0L,
                                    title = title.trim(),
                                    description = description.trim(),
                                    servings = validServings,
                                    totalMinutes = validMinutes,
                                    ingredients = ingredients,
                                    steps = steps
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
    @Composable
    fun RecipeDetailScreen(
        recipe: Recipe,
        onBack: () -> Unit,
        modifier: Modifier = Modifier
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

                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.headlineLarge
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



}