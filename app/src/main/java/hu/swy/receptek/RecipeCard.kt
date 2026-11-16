package hu.swy.receptek

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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