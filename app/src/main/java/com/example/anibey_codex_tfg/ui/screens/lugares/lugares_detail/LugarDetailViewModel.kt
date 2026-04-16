package com.example.anibey_codex_tfg.ui.screens.lugares.lugares_detail

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
class LugarDetailViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<LugarDetailState>(LugarDetailState.Loading)
    val uiState: StateFlow<LugarDetailState> = _uiState.asStateFlow()

    fun cargarLugar(lugarId: String) {
        viewModelScope.launch {
            _uiState.value = LugarDetailState.Loading
            db.collection("lugar").document(lugarId).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        @Suppress("UNCHECKED_CAST")
                        val personajesList = doc.get("personajes") as? List<String> ?: emptyList()
                        @Suppress("UNCHECKED_CAST")
                        val monstruosList = doc.get("monstruos") as? List<String> ?: emptyList()

                        val lugar = Lugar(
                            id = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            descripcion = doc.getString("descripcion") ?: "",
                            tipo = doc.getString("tipo") ?: "",
                            region = doc.getString("region") ?: "",
                            imagenURL = doc.getString("imagenURL") ?: "",
                            personajes = personajesList,
                            monstruos = monstruosList
                        )
                        _uiState.value = LugarDetailState.Success(lugar)
                    } else {
                        _uiState.value = LugarDetailState.Error("El lugar no existe")
                    }
                }
                .addOnFailureListener { e ->
                    _uiState.value = LugarDetailState.Error(e.message ?: "Error desconocido")
                }
        }
    }
}
