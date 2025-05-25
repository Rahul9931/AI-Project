package com.example.ai_project.chat_app.view_model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai_project.chat_app.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChatRepository(application)
    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Loading)
    val uiState: StateFlow<ChatUiState> = _uiState

    init {
        initializeModel()
    }

    private fun initializeModel() {
        viewModelScope.launch {
            _uiState.value = ChatUiState.Loading
            repository.initializeModel().fold(
                onSuccess = {
                    _uiState.value = ChatUiState.Ready
                },
                onFailure = { e ->
                    _uiState.value = ChatUiState.Error(e.message ?: "Unknown error")
                }
            )
        }
    }

    fun sendMessage(prompt: String) {
        viewModelScope.launch {
            _uiState.value = ChatUiState.Generating(prompt)
            Log.d("ChatViewModel", "State changed to Generating")

            repository.generateResponse(prompt).fold(
                onSuccess = { response ->
                    _uiState.value = ChatUiState.MessageReceived(response)
                    Log.d("ChatViewModel", "State changed to MessageReceived: $response")
                },
                onFailure = { e ->
                    _uiState.value = ChatUiState.Error(e.message ?: "Generation failed")
                    Log.e("ChatViewModel", "State changed to Error", e)
                }
            )
        }
    }

    public override fun onCleared() {
        repository.cleanup()
        super.onCleared()
    }

    sealed class ChatUiState {
        object Loading : ChatUiState()
        object Ready : ChatUiState()
        data class Generating(val prompt: String) : ChatUiState()
        data class MessageReceived(val response: String) : ChatUiState()
        data class Error(val message: String) : ChatUiState()
    }
}