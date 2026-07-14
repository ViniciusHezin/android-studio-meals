package com.example.mbd2

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object NetworkManager {

    // Task 7: Search by meal name
    suspend fun searchMealsByName(name: String): List<MealEntity> {
        val urlString = "https://www.themealdb.com/api/json/v1/1/search.php?s=$name"
        return fetchAndParseMeals(urlString)
    }

    // Task 3: Search by ingredient — fetches IDs first, then full details per meal
    suspend fun searchMealsByIngredient(ingredient: String): List<MealEntity> {
        return withContext(Dispatchers.IO) {
            val filterUrl = "https://www.themealdb.com/api/json/v1/1/filter.php?i=$ingredient"
            val response = makeHttpRequest(filterUrl)

            if (response.isNullOrEmpty()) return@withContext emptyList()

            val jsonObject = JSONObject(response)
            if (!jsonObject.has("meals") || jsonObject.isNull("meals")) return@withContext emptyList()

            val mealsArray = jsonObject.getJSONArray("meals")
            val fullMealsList = mutableListOf<MealEntity>()

            for (i in 0 until mealsArray.length()) {
                val mealId = mealsArray.getJSONObject(i).getString("idMeal")
                val lookupUrl = "https://www.themealdb.com/api/json/v1/1/lookup.php?i=$mealId"
                val fullMeal = fetchAndParseMeals(lookupUrl).firstOrNull()
                if (fullMeal != null) fullMealsList.add(fullMeal)
            }
            return@withContext fullMealsList
        }
    }

    private suspend fun fetchAndParseMeals(urlString: String): List<MealEntity> {
        return withContext(Dispatchers.IO) {
            val response = makeHttpRequest(urlString)
            if (response.isNullOrEmpty()) return@withContext emptyList()

            val parsedMeals = mutableListOf<MealEntity>()
            try {
                val jsonObject = JSONObject(response)
                if (jsonObject.has("meals") && !jsonObject.isNull("meals")) {
                    val mealsArray = jsonObject.getJSONArray("meals")
                    for (i in 0 until mealsArray.length()) {
                        parsedMeals.add(parseSingleMeal(mealsArray.getJSONObject(i)))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext parsedMeals
        }
    }

    private fun makeHttpRequest(urlString: String): String? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) sb.append(line)
                reader.close()
                sb.toString()
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            connection?.disconnect()
        }
    }

    private fun parseSingleMeal(json: JSONObject): MealEntity {
        fun str(key: String): String? =
            if (json.has(key) && !json.isNull(key) && json.getString(key).isNotBlank())
                json.getString(key) else null

        return MealEntity(
            mealName = json.getString("strMeal"),
            drinkAlternate = str("strDrinkAlternate"),
            category = str("strCategory"),
            area = str("strArea"),
            instructions = str("strInstructions"),
            mealThumb = str("strMealThumb"),
            tags = str("strTags"),
            youtube = str("strYoutube"),
            ingredient1 = str("strIngredient1"),   ingredient2 = str("strIngredient2"),
            ingredient3 = str("strIngredient3"),   ingredient4 = str("strIngredient4"),
            ingredient5 = str("strIngredient5"),   ingredient6 = str("strIngredient6"),
            ingredient7 = str("strIngredient7"),   ingredient8 = str("strIngredient8"),
            ingredient9 = str("strIngredient9"),   ingredient10 = str("strIngredient10"),
            ingredient11 = str("strIngredient11"), ingredient12 = str("strIngredient12"),
            ingredient13 = str("strIngredient13"), ingredient14 = str("strIngredient14"),
            ingredient15 = str("strIngredient15"), ingredient16 = str("strIngredient16"),
            ingredient17 = str("strIngredient17"), ingredient18 = str("strIngredient18"),
            ingredient19 = str("strIngredient19"), ingredient20 = str("strIngredient20"),
            measure1 = str("strMeasure1"),   measure2 = str("strMeasure2"),
            measure3 = str("strMeasure3"),   measure4 = str("strMeasure4"),
            measure5 = str("strMeasure5"),   measure6 = str("strMeasure6"),
            measure7 = str("strMeasure7"),   measure8 = str("strMeasure8"),
            measure9 = str("strMeasure9"),   measure10 = str("strMeasure10"),
            measure11 = str("strMeasure11"), measure12 = str("strMeasure12"),
            measure13 = str("strMeasure13"), measure14 = str("strMeasure14"),
            measure15 = str("strMeasure15"), measure16 = str("strMeasure16"),
            measure17 = str("strMeasure17"), measure18 = str("strMeasure18"),
            measure19 = str("strMeasure19"), measure20 = str("strMeasure20")
        )
    }
}