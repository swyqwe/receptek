package hu.swy.receptek

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp



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


