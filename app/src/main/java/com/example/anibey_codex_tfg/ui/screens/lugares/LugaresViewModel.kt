package com.example.anibey_codex_tfg.ui.screens.lugares

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anibey_codex_tfg.domain.model.Lugar
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estados posibles de la pantalla de Lugares


@HiltViewModel
class LugaresViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<LugaresStates>(LugaresStates.Loading)
    val uiState: StateFlow<LugaresStates> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Datos en memoria para filtrado
    private var allLugares: List<Lugar> = emptyList()

    init {
        cargarLugares()
    }

    private fun cargarLugares() {
        viewModelScope.launch {
            _uiState.value = LugaresStates.Loading
            try {
                db.collection("lugares")
                    .get()
                    .addOnSuccessListener { documents ->
                        val lugares = documents.mapNotNull { doc ->
                            try {
                                Lugar(
                                    id = doc.id,
                                    nombre = doc.getString("nombre") ?: "",
                                    descripcion = doc.getString("descripcion") ?: "",
                                    tipo = doc.getString("tipo") ?: "",
                                    region = doc.getString("region") ?: "",
                                    imagenURL = doc.getString("imagenURL") ?: "",
                                    personajes = doc.get("personajes") as? List<String> ?: emptyList(),
                                    monstruos = doc.get("monstruos") as? List<String> ?: emptyList()
                                )
                            } catch (e: Exception) {
                                Log.e("LugaresViewModel", "Error mapeando lugar: ${e.message}")
                                null
                            }
                        }
                        allLugares = lugares
                        _uiState.value = if (lugares.isEmpty()) {
                            LugaresStates.Empty
                        } else {
                            LugaresStates.Success(lugares)
                        }
                    }
                    .addOnFailureListener { exception ->
                        _uiState.value = LugaresStates.Error(
                            "Error al cargar lugares: ${exception.message}"
                        )
                        Log.e("LugaresViewModel", "Error cargando lugares", exception)
                    }
            } catch (e: Exception) {
                _uiState.value = LugaresStates.Error(e.message ?: "Error desconocido")
                Log.e("LugaresViewModel", "Excepción al cargar lugares", e)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun getLugaresFiltrados(): List<Lugar> {
        val query = _searchQuery.value.lowercase()
        return if (query.isEmpty()) {
            allLugares
        } else {
            allLugares.filter { lugar ->
                lugar.nombre.lowercase().contains(query) ||
                lugar.descripcion.lowercase().contains(query) ||
                lugar.tipo.lowercase().contains(query) ||
                lugar.region.lowercase().contains(query)
            }
        }
    }

    fun recargarLugares() {
        cargarLugares()
    }
}

