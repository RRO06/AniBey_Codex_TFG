package com.example.anibey_codex_tfg.ui.screens.bestiario.monstruo_detail

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
class MonstruoDetailViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<MonstruoDetailStates>(MonstruoDetailStates.Loading)
    val uiState: StateFlow<MonstruoDetailStates> = _uiState.asStateFlow()

    fun getMonstruo(id: String) {
        viewModelScope.launch {
            _uiState.value = MonstruoDetailStates.Loading
            db.collection("monstruos").document(id)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val monstruo = document.toObject(Monstruo::class.java)
                        if (monstruo != null) {
                            _uiState.value = MonstruoDetailStates.Success(monstruo)
                        } else {
                            _uiState.value = MonstruoDetailStates.Error("No se pudo procesar la información")
                        }
                    } else {
                        _uiState.value = MonstruoDetailStates.Error("Monstruo no encontrado")
                    }
                }
                .addOnFailureListener { e ->
                    _uiState.value = MonstruoDetailStates.Error(e.message ?: "Error desconocido")
                }
        }
    }
}
