package hu.swy.receptek

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


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



