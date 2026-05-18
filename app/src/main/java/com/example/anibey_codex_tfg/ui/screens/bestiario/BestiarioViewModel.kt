package com.example.anibey_codex_tfg.ui.screens.bestiario

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anibey_codex_tfg.domain.model.Monstruo
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BestiarioViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<BestiarioStates>(BestiarioStates.Loading)
    val uiState: StateFlow<BestiarioStates> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var allMonstruos: List<Monstruo> = emptyList()

    init {
        cargarBestiario()
    }

    fun cargarBestiario() {
        viewModelScope.launch {
            _uiState.value = BestiarioStates.Loading
            try {
                db.collection("monstruos")
                    .get()
                    .addOnSuccessListener { documents ->
                        val lista = documents.mapNotNull { doc ->
                            try {
                                doc.toObject(Monstruo::class.java).copy(id = doc.id)
                            } catch (e: Exception) {
                                Log.e("BestiarioViewModel", "Error al convertir documento: ${e.message}")
                                null
                            }
                        }
                        allMonstruos = lista
                        actualizarEstado()
                    }
                    .addOnFailureListener { exception ->
                        Log.e("BestiarioViewModel", "Error Firestore: ${exception.message}")
                        _uiState.value = BestiarioStates.Error("Error: ${exception.message}")
                    }
            } catch (e: Exception) {
                _uiState.value = BestiarioStates.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        actualizarEstado()
    }

    private fun actualizarEstado() {
        val query = _searchQuery.value.lowercase().trim()
        val filtrados = if (query.isEmpty()) {
            allMonstruos
        } else {
            allMonstruos.filter { 
                it.nombre.lowercase().contains(query) || 
                it.categoria.lowercase().contains(query) 
            }
        }

        _uiState.value = when {
            allMonstruos.isEmpty() -> BestiarioStates.Empty
            else -> BestiarioStates.Success(filtrados)
        }
    }
}
