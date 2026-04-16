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
                db.collection("lugar")
                    .get()
                    .addOnSuccessListener { documents ->
                        val lugares = documents.mapNotNull { doc ->
                            try {
                                @Suppress("UNCHECKED_CAST")
                                val personajesList = doc.get("personajes") as? List<String> ?: emptyList()
                                @Suppress("UNCHECKED_CAST")
                                val monstruosList = doc.get("monstruos") as? List<String> ?: emptyList()

                                Lugar(
                                    id = doc.id,
                                    nombre = doc.getString("nombre") ?: "",
                                    descripcion = doc.getString("descripcion") ?: "",
                                    tipo = doc.getString("tipo") ?: "",
                                    region = doc.getString("region") ?: "",
                                    imagenURL = doc.getString("imagenURL") ?: "",
                                    personajes = personajesList,
                                    monstruos = monstruosList
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        allLugares = lugares
                        _uiState.value = if (lugares.isEmpty()) LugaresStates.Empty else LugaresStates.Success(lugares)
                    }
                    .addOnFailureListener { exception ->
                        _uiState.value = LugaresStates.Error("Error al cargar lugares: ${exception.message}")
                    }
            } catch (e: Exception) {
                _uiState.value = LugaresStates.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun getLugaresFiltrados(): List<Lugar> {
        val query = _searchQuery.value.lowercase().trim()
        return if (query.isEmpty()) {
            allLugares
        } else {
            // Filtrar SOLO por nombre como has pedido
            allLugares.filter { lugar ->
                lugar.nombre.lowercase().contains(query)
            }
        }
    }

    fun recargarLugares() {
        cargarLugares()
    }
}
