package hu.swy.receptek

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import hu.swy.receptek.data.RecipeDatabase
import hu.swy.receptek.data.RecipeRepository

// Összeköti a ViewModelt a képernyőkkel, és kezeli a képernyőváltást.
@Composable
fun RecipeApp() {
    val appContext = LocalContext.current.applicationContext

    // A függőségeket itt állítjuk össze, nem a ViewModelen belül.
    val factory = remember(appContext) {
        viewModelFactory {
            initializer {
                val database = RecipeDatabase.getInstance(appContext)
                val repository = RecipeRepository(database.recipeDao())

                RecipeViewModel(repository)
            }
        }
    }

    val recipeViewModel: RecipeViewModel = viewModel(
        factory = factory
    )

    val uiState by recipeViewModel.uiState.collectAsStateWithLifecycle()
    val recipes = uiState.recipes

    var selectedRecipeId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }

    var selectedCategory by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var isFormOpen by rememberSaveable {
        mutableStateOf(false)
    }

    var editingRecipeId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    var pendingDeleteId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    val availableCategories = (
            defaultRecipeCategories +
                    recipes.map { it.category }.sorted()
            ).filter { it.isNotBlank() }
        .distinct()

    val selectedRecipe = recipes.find {
        it.id == selectedRecipeId
    }

    val editingRecipe = recipes.find {
        it.id == editingRecipeId
    }

    val pendingDeleteRecipe = recipes.find {
        it.id == pendingDeleteId
    }

    // A hiba megjelenítése után töröljük a feldolgozott üzenetet.
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(
                appContext,
                message,
                Toast.LENGTH_LONG
            ).show()

            recipeViewModel.clearError()
        }
    }

    // Sikeres művelet után rendezzük a képernyők állapotát.
    LaunchedEffect(uiState.completedOperation) {
        val operation = uiState.completedOperation
            ?: return@LaunchedEffect

        when (operation) {
            RecipeOperation.SAVE -> {
                isFormOpen = false
                editingRecipeId = null
            }

            RecipeOperation.DELETE -> {
                selectedRecipeId = null
                pendingDeleteId = null
            }
        }

        recipeViewModel.consumeCompletedOperation()
    }

    // A törléshez továbbra is külön megerősítést kérünk.
    if (pendingDeleteRecipe != null) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isDeleting) {
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
                    enabled = !uiState.isDeleting,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    onClick = {
                        recipeViewModel.deleteRecipe(pendingDeleteRecipe)
                    }
                ) {
                    Text(
                        text = if (uiState.isDeleting) {
                            "Törlés..."
                        } else {
                            "Törlés"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !uiState.isDeleting,
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
        uiState.isLoading -> {
            Scaffold { innerPadding ->
                Text(
                    text = "Receptek betöltése...",
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(16.dp)
                )
            }
        }

        isFormOpen -> {
            BackHandler {
                if (!uiState.isSaving) {
                    isFormOpen = false
                    editingRecipeId = null
                }
            }

            // Nem nyitunk véletlenül új receptet egy hiányzó régi helyett.
            if (editingRecipeId != null && editingRecipe == null) {
                Scaffold { innerPadding ->
                    TextButton(
                        onClick = {
                            isFormOpen = false
                            editingRecipeId = null
                            selectedRecipeId = null
                        },
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        Text(text = "A recept nem található. Vissza a listához")
                    }
                }
            } else {
                RecipeFormScreen(
                    initialRecipe = editingRecipe,
                    availableCategories = availableCategories,
                    onSave = { recipe ->
                        recipeViewModel.saveRecipe(recipe)
                    },
                    onCancel = {
                        if (!uiState.isSaving) {
                            isFormOpen = false
                            editingRecipeId = null
                        }
                    }
                )
            }
        }

        selectedRecipe != null -> {
            BackHandler {
                if (!uiState.isDeleting) {
                    selectedRecipeId = null
                }
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
                onSearchQueryChange = {
                    searchQuery = it
                },
                selectedCategory = selectedCategory,
                onCategoryChange = {
                    selectedCategory = it
                }
            )
        }
    }
}