package io.github.hziqzmin.wildguardai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay // Only for simulation, replace with real loading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    // Private state flow that holds the loading status (true = loading, false = done)
    private val _isLoading = MutableStateFlow(true)
    // Public immutable state flow that the UI can observe
    val isLoading = _isLoading.asStateFlow()

    init {
        // Start loading data as soon as the ViewModel is created
        loadData()
    }

    // Function to simulate (or actually perform) data loading
    private fun loadData() {
        viewModelScope.launch {
            // --- IMPORTANT ---
            // Replace this 'delay' with your actual model loading logic!
            // For example: loadLlmModel(), loadEmbeddings(), etc.
            // This might take several seconds.
            delay(3000) // Simulate loading for 3 seconds

            // Once loading is complete, update the state flow
            _isLoading.value = false
            // ---------------
        }
    }
}