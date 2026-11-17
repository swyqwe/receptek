package hu.swy.receptek

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview


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