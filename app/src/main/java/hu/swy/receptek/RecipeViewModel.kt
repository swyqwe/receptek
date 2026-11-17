package hu.swy.receptek

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.swy.receptek.data.RecipeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// A sikeresen befejezett adatbázis-művelet típusa.
enum class RecipeOperation {
    SAVE,
    DELETE
}

// A felület számára szükséges adatok és műveleti állapotok.
data class RecipeUiState(
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val completedOperation: RecipeOperation? = null
)

// Kezeli a receptek betöltését és az adatbázis-műveletek állapotait.
class RecipeViewModel(
    private val repository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeRecipes()
    }

    // Folyamatosan követi az adatbázisban tárolt recepteket.
    private fun observeRecipes() {
        viewModelScope.launch {
            try {
                repository.recipes.collect { recipes ->
                    _uiState.update { state ->
                        state.copy(
                            recipes = recipes,
                            isLoading = false
                        )
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = "Nem sikerült betölteni a recepteket."
                    )
                }
            }
        }
    }

    // Elmenti az új vagy szerkesztett receptet.
    fun saveRecipe(recipe: Recipe) {
        runOperation(RecipeOperation.SAVE) {
            repository.save(recipe)
        }
    }

    // Törli a felület által megadott receptet.
    fun deleteRecipe(recipe: Recipe) {
        runOperation(RecipeOperation.DELETE) {
            repository.delete(recipe)
        }
    }

    // Egyszerre csak egy írási műveletet indít, közös hibakezeléssel.
    private fun runOperation(
        operation: RecipeOperation,
        action: suspend () -> Unit
    ) {
        val current = _uiState.value

        if (
            current.isSaving ||
            current.isDeleting ||
            current.completedOperation != null
        ) {
            return
        }

        _uiState.update { state ->
            state.copy(
                isSaving = operation == RecipeOperation.SAVE,
                isDeleting = operation == RecipeOperation.DELETE,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                action()

                _uiState.update { state ->
                    state.copy(completedOperation = operation)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update { state ->
                    state.copy(
                        errorMessage = when (operation) {
                            RecipeOperation.SAVE ->
                                "Nem sikerült menteni a receptet."

                            RecipeOperation.DELETE ->
                                "Nem sikerült törölni a receptet."
                        }
                    )
                }
            } finally {
                _uiState.update { state ->
                    state.copy(
                        isSaving = false,
                        isDeleting = false
                    )
                }
            }
        }
    }

    // A felület jelzi, hogy megjelenítette a hibát.
    fun clearError() {
        _uiState.update { state ->
            state.copy(errorMessage = null)
        }
    }

    // A felület jelzi, hogy feldolgozta a sikeres műveletet.
    fun consumeCompletedOperation() {
        _uiState.update { state ->
            state.copy(completedOperation = null)
        }
    }
}