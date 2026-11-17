package hu.swy.receptek

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp



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



