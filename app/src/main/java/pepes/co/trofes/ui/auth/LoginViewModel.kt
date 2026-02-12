package pepes.co.trofes.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pepes.co.trofes.auth.AuthRepositoryV1
import pepes.co.trofes.data.remote.AuthResponseV1

class LoginViewModel(
    private val authRepository: AuthRepositoryV1,
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun login(login: String, password: String) {
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            val result = authRepository.login(login, password)
            _loginState.value = if (result.isSuccess) {
                LoginState.Success(requireNotNull(result.getOrNull()))
            } else {
                LoginState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun googleLogin(idToken: String) {
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            val result = authRepository.googleLogin(idToken)
            _loginState.value = if (result.isSuccess) {
                LoginState.Success(requireNotNull(result.getOrNull()))
            } else {
                LoginState.Error(result.exceptionOrNull()?.message ?: "Google login failed")
            }
        }
    }

    fun register(username: String, email: String, password: String, confirmation: String) {
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            val result = authRepository.register(username, email, password, confirmation)
            _loginState.value = if (result.isSuccess) {
                LoginState.Success(requireNotNull(result.getOrNull()))
            } else {
                LoginState.Error(result.exceptionOrNull()?.message ?: "Register failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}

sealed class LoginState {
    data object Loading : LoginState()
    data class Success(val authResponse: AuthResponseV1) : LoginState()
    data class Error(val message: String) : LoginState()
}
