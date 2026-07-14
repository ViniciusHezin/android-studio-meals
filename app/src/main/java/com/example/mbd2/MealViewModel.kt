package com.example.mbd2

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MealViewModel(private val dao: MealDao) : ViewModel() {

    // Which screen we're on — survives rotation
    var currentScreen by mutableStateOf("MENU")

    // Search results shown on screen
    var searchResults by mutableStateOf<List<MealEntity>>(emptyList())

    // The user's typed query
    var searchQuery by mutableStateOf("")

    // Loading spinner flag
    var isLoading by mutableStateOf(false)

    // Task 3: Search web by ingredient
    fun searchWebByIngredient(ingredient: String) {
        isLoading = true
        viewModelScope.launch {
            searchResults = NetworkManager.searchMealsByIngredient(ingredient)
            isLoading = false
        }
    }

    // Task 4: Save current web results to DB
    fun saveCurrentResultsToDb() {
        viewModelScope.launch {
            if (searchResults.isNotEmpty()) {
                dao.insertMeals(searchResults)
            }
        }
    }

    // Task 5: Search local DB by name or any ingredient
    fun searchDatabase(query: String) {
        isLoading = true
        viewModelScope.launch {
            searchResults = dao.searchMealsByNameOrIngredient(query)
            isLoading = false
        }
    }

    // Task 7: Search web by meal name
    fun searchWebByName(name: String) {
        isLoading = true
        viewModelScope.launch {
            searchResults = NetworkManager.searchMealsByName(name)
            isLoading = false
        }
    }

    // Go back to menu and clear state
    fun resetSearchAndGoToMenu() {
        searchResults = emptyList()
        searchQuery = ""
        currentScreen = "MENU"
    }
}

class MealViewModelFactory(private val dao: MealDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MealViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MealViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}