package com.neuromesh.crisis.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuromesh.crisis.data.model.MeshPeer
import com.neuromesh.crisis.data.repository.MeshRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class MeshViewModel @Inject constructor(
    private val meshRepository: MeshRepository
) : ViewModel() {

    val peers: StateFlow<List<MeshPeer>> = meshRepository.peers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val connectedCount: StateFlow<Int> = meshRepository.connectedPeers
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val meshStrength: StateFlow<MeshStrength> = connectedCount.map { count ->
        when {
            count == 0 -> MeshStrength.ISOLATED
            count == 1 -> MeshStrength.WEAK
            count <= 3 -> MeshStrength.MODERATE
            else -> MeshStrength.STRONG
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MeshStrength.ISOLATED)
}

enum class MeshStrength { ISOLATED, WEAK, MODERATE, STRONG }
