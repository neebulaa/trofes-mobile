package pepes.co.trofes.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pepes.co.trofes.auth.AuthRepositoryV1

class LoginViewModelFactory(
    private val authRepository: AuthRepositoryV1,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(authRepository) as T
            }
            else -> {
                throw IllegalArgumentException(
                    "Unknown ViewModel class: ${modelClass.name}. " +
                        "Expected ${LoginViewModel::class.java.name}"
                )
            }
        }
    }
}
