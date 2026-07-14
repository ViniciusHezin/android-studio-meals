package com.example.mbd2

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dao = AppDatabase.getDatabase(applicationContext).mealDao()
        val factory = MealViewModelFactory(dao)
        val viewModel = ViewModelProvider(this, factory)[MealViewModel::class.java]

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation(viewModel)
                }
            }
        }
    }
}

// Task 6: Native image loader — no Glide/Coil
@Composable
fun NativeImage(urlString: String?, modifier: Modifier = Modifier) {
    var bitmap by remember(urlString) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(urlString) {
        if (!urlString.isNullOrBlank()) {
            withContext(Dispatchers.IO) {
                try {
                    val connection = URL(urlString).openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connect()
                    bitmap = BitmapFactory.decodeStream(connection.inputStream)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "Meal Image",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(modifier = modifier.background(Color.LightGray))
    }
}

// Navigation controller
@Composable
fun AppNavigation(viewModel: MealViewModel) {
    when (viewModel.currentScreen) {
        "MENU"              -> MainMenuScreen(viewModel)
        "SEARCH_INGREDIENT" -> SearchScreen(
            viewModel = viewModel,
            title = "Search by Ingredient (Web)",
            onSearch = { viewModel.searchWebByIngredient(it) },
            showSaveButton = true
        )
        "SEARCH_DB"         -> SearchScreen(
            viewModel = viewModel,
            title = "Search Database",
            onSearch = { viewModel.searchDatabase(it) },
            showSaveButton = false
        )
        "SEARCH_NAME"       -> SearchScreen(
            viewModel = viewModel,
            title = "Search by Name (Web)",
            onSearch = { viewModel.searchWebByName(it) },
            showSaveButton = false
        )
    }
}

// Task 1 + Task 7: Main menu with all 4 buttons
@Composable
fun MainMenuScreen(viewModel: MealViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val dao = AppDatabase.getDatabase(context).mealDao()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Task 2
        Button(
            onClick = {
                coroutineScope.launch {
                    dao.insertMeals(HardcodedData.meals)
                    Toast.makeText(context, "Meals added to DB!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f).padding(8.dp)
        ) { Text("Add Meals to DB") }

        // Task 3
        Button(
            onClick = { viewModel.currentScreen = "SEARCH_INGREDIENT" },
            modifier = Modifier.fillMaxWidth(0.8f).padding(8.dp)
        ) { Text("Search for Meals By Ingredient") }

        // Task 5
        Button(
            onClick = { viewModel.currentScreen = "SEARCH_DB" },
            modifier = Modifier.fillMaxWidth(0.8f).padding(8.dp)
        ) { Text("Search for Meals") }

        // Task 7
        Button(
            onClick = { viewModel.currentScreen = "SEARCH_NAME" },
            modifier = Modifier.fillMaxWidth(0.8f).padding(8.dp)
        ) { Text("Search Meals by Name (Web)") }
    }
}

// Reusable search screen for Tasks 3, 5, 7
@Composable
fun SearchScreen(
    viewModel: MealViewModel,
    title: String,
    onSearch: (String) -> Unit,
    showSaveButton: Boolean
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.searchQuery = it },
            label = { Text("Type here...") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { onSearch(viewModel.searchQuery) }) { Text("Search") }

            if (showSaveButton) {
                Button(onClick = {
                    viewModel.saveCurrentResultsToDb()
                    Toast.makeText(context, "Saved to DB!", Toast.LENGTH_SHORT).show()
                }) { Text("Save meals to Database") }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(viewModel.searchResults) { meal ->
                    MealCard(meal)
                }
            }
        }

        Button(
            onClick = { viewModel.resetSearchAndGoToMenu() },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) { Text("Back to Menu") }
    }
}

// Task 6: Meal card with thumbnail image and all ingredients
@Composable
fun MealCard(meal: MealEntity) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Task 6: thumbnail image
            NativeImage(
                urlString = meal.mealThumb,
                modifier = Modifier.fillMaxWidth().height(180.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("Meal: ${meal.mealName}", fontWeight = FontWeight.Bold)
            Text("DrinkAlternate: ${meal.drinkAlternate ?: "null"}")
            Text("Category: ${meal.category ?: "N/A"}")
            Text("Area: ${meal.area ?: "N/A"}")
            Text("Tags: ${meal.tags ?: "N/A"}")
            Text("Youtube: ${meal.youtube ?: "N/A"}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("Instructions:", fontWeight = FontWeight.Bold)
            Text(meal.instructions ?: "N/A")

            Spacer(modifier = Modifier.height(8.dp))
            Text("Ingredients:", fontWeight = FontWeight.Bold)

            // All 20 ingredients
            val ingredientsAndMeasures = listOf(
                meal.ingredient1 to meal.measure1,   meal.ingredient2 to meal.measure2,
                meal.ingredient3 to meal.measure3,   meal.ingredient4 to meal.measure4,
                meal.ingredient5 to meal.measure5,   meal.ingredient6 to meal.measure6,
                meal.ingredient7 to meal.measure7,   meal.ingredient8 to meal.measure8,
                meal.ingredient9 to meal.measure9,   meal.ingredient10 to meal.measure10,
                meal.ingredient11 to meal.measure11, meal.ingredient12 to meal.measure12,
                meal.ingredient13 to meal.measure13, meal.ingredient14 to meal.measure14,
                meal.ingredient15 to meal.measure15, meal.ingredient16 to meal.measure16,
                meal.ingredient17 to meal.measure17, meal.ingredient18 to meal.measure18,
                meal.ingredient19 to meal.measure19, meal.ingredient20 to meal.measure20
            ).filter { !it.first.isNullOrBlank() }

            ingredientsAndMeasures.forEachIndexed { index, (ing, meas) ->
                Text("Ingredient${index + 1}: $ing")
                Text("Measure${index + 1}: ${meas ?: ""}")
            }
        }
    }
}