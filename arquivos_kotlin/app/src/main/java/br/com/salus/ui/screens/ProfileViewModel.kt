package br.com.salus.ui.screens


import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val username: String = "",
    val email: String = "",
    val profileImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ProfileViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Aqui você carregaria os dados do usuário de:
                // - SharedPreferences
                // - Room Database
                // - API REST
                // - Firebase
                
                // Exemplo com dados mockados:
                _uiState.update {
                    it.copy(
                        username = "Usuário",
                        email = "blablabla@gmail.com",
                        isLoading = false
                    )
                }
                
                // Exemplo real com SharedPreferences:
                // val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                // val username = sharedPreferences.getString("username", "") ?: ""
                // val email = sharedPreferences.getString("email", "") ?: ""
                // val imageUriString = sharedPreferences.getString("profile_image_uri", null)
                // val imageUri = imageUriString?.let { Uri.parse(it) }
                //
                // _uiState.update {
                //     it.copy(
                //         username = username,
                //         email = email,
                //         profileImageUri = imageUri,
                //         isLoading = false
                //     )
                // }
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Erro ao carregar perfil: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateUsername(newUsername: String) {
        _uiState.update { it.copy(username = newUsername) }
    }

    fun updateProfileImage(uri: Uri) {
        _uiState.update { it.copy(profileImageUri = uri) }
    }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val currentState = _uiState.value
                
                // Validação
                if (currentState.username.isBlank()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Por favor, insira um nome de usuário"
                        )
                    }
                    return@launch
                }

                // Aqui você salvaria os dados:
                // - SharedPreferences
                // - Room Database
                // - API REST
                // - Firebase
                
                // Exemplo com SharedPreferences:
                // val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                // val editor = sharedPreferences.edit()
                // editor.putString("username", currentState.username)
                // currentState.profileImageUri?.let {
                //     editor.putString("profile_image_uri", it.toString())
                // }
                // editor.apply()

                // Simular salvamento
                delay(500)
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Dados salvos com sucesso!"
                    )
                }
                
                // Limpar mensagem de sucesso após 3 segundos
                delay(3000)
                _uiState.update { it.copy(successMessage = null) }
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Erro ao salvar perfil: ${e.message}"
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                // Limpar dados de sessão:
                // - SharedPreferences
                // - Room Database
                // - Tokens de autenticação
                
                // Exemplo com SharedPreferences:
                // val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                // val editor = sharedPreferences.edit()
                // editor.clear()
                // editor.apply()
                
                // Resetar estado
                _uiState.update { ProfileUiState() }
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Erro ao fazer logout: ${e.message}")
                }
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
