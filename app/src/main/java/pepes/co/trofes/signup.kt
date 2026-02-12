package pepes.co.trofes

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pepes.co.trofes.auth.AuthRepositoryV1
import pepes.co.trofes.auth.AuthSession
import pepes.co.trofes.data.local.TokenManager
import pepes.co.trofes.data.remote.RetrofitClient
import pepes.co.trofes.ui.auth.LoginState
import pepes.co.trofes.ui.auth.LoginViewModel
import pepes.co.trofes.ui.auth.LoginViewModelFactory

class SignupActivity : AppCompatActivity() {
    private var passwordVisible = false
    private var confirmPasswordVisible = false

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirm: EditText
    private lateinit var btnSignup: Button

    private val viewModel: LoginViewModel by viewModels {
        val tokenManager = TokenManager(this)
        val authSession = AuthSession(this)
        val repo = AuthRepositoryV1(
            apiService = RetrofitClient.apiServiceV1,
            tokenManager = tokenManager,
            authSession = authSession,
        )
        LoginViewModelFactory(repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirm = findViewById(R.id.etConfirmPassword)
        btnSignup = findViewById(R.id.btnSignup)

        btnSignup.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirm = etConfirm.text.toString()

            // reset error
            etUsername.error = null
            etEmail.error = null
            etPassword.error = null
            etConfirm.error = null

            // basic validation (client)
            if (username.isEmpty()) {
                etUsername.error = "Username harus diisi"
                etUsername.requestFocus()
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                etEmail.error = "Email harus diisi"
                etEmail.requestFocus()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Format email tidak valid"
                etEmail.requestFocus()
                return@setOnClickListener
            }
            if (password.isBlank()) {
                etPassword.error = "Password harus diisi"
                etPassword.requestFocus()
                return@setOnClickListener
            }
            if (confirm.isBlank()) {
                etConfirm.error = "Konfirmasi password harus diisi"
                etConfirm.requestFocus()
                return@setOnClickListener
            }
            if (password != confirm) {
                etConfirm.error = "Konfirmasi password tidak sama"
                etConfirm.requestFocus()
                return@setOnClickListener
            }

            viewModel.register(username, email, password, confirm)
        }

        findViewById<TextView>(R.id.tvLoginLink).setOnClickListener {
            startActivity(Intent(this, SigninActivity::class.java))
            finish()
        }

        // Password visibility toggle
        val ivTogglePassword = findViewById<ImageView>(R.id.ivTogglePassword)
        val ivToggleConfirm = findViewById<ImageView>(R.id.ivToggleConfirm)

        ivTogglePassword.setOnClickListener {
            passwordVisible = !passwordVisible
            if (passwordVisible) {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivTogglePassword.setImageResource(R.mipmap.ic_eyeon_foreground)
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivTogglePassword.setImageResource(R.mipmap.ic_eyeoff_foreground)
            }
            etPassword.setSelection(etPassword.text?.length ?: 0)
        }

        ivToggleConfirm.setOnClickListener {
            confirmPasswordVisible = !confirmPasswordVisible
            if (confirmPasswordVisible) {
                etConfirm.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivToggleConfirm.setImageResource(R.mipmap.ic_eyeon_foreground)
            } else {
                etConfirm.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivToggleConfirm.setImageResource(R.mipmap.ic_eyeoff_foreground)
            }
            etConfirm.setSelection(etConfirm.text?.length ?: 0)
        }

        observeRegisterState()
    }

    private fun observeRegisterState() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Loading -> {
                    btnSignup.isEnabled = false
                }

                is LoginState.Success -> {
                    btnSignup.isEnabled = true
                    Toast.makeText(this, "Register berhasil", Toast.LENGTH_SHORT).show()
                    // register berhasil -> onboarding page
                    startActivity(Intent(this, Onboarding_Isi_Profile::class.java))
                    finishAffinity()
                }

                is LoginState.Error -> {
                    btnSignup.isEnabled = true
                    val applied = applyFieldErrorsIfAny(state.message)
                    if (!applied) Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun applyFieldErrorsIfAny(rawMessage: String): Boolean {
        return try {
            val jsonStart = rawMessage.indexOf('{')
            if (jsonStart < 0) return false

            val json = org.json.JSONObject(rawMessage.substring(jsonStart))
            if (!json.has("errors")) return false

            val errors = json.getJSONObject("errors")
            var applied = false

            fun firstError(key: String): String? {
                if (!errors.has(key)) return null
                val arr = errors.optJSONArray(key) ?: return null
                return arr.optString(0).takeIf { it.isNotBlank() }
            }

            firstError("username")?.let {
                etUsername.error = it
                applied = true
            }
            firstError("email")?.let {
                etEmail.error = it
                applied = true
            }
            firstError("password")?.let {
                etPassword.error = it
                applied = true
            }
            firstError("password_confirmation")?.let {
                etConfirm.error = it
                applied = true
            }

            applied
        } catch (_: Exception) {
            false
        }
    }

    // validateSignupForm() sudah tidak dipakai lagi (diganti api). Biarkan kalau masih dipanggil dari tempat lain.
    private fun validateSignupForm(): Boolean {
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirm = findViewById<EditText>(R.id.etConfirmPassword)

        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val confirm = etConfirm.text.toString()

        // reset error
        etUsername.error = null
        etEmail.error = null
        etPassword.error = null
        etConfirm.error = null

        if (username.isEmpty()) {
            etUsername.error = "Username harus diisi"
            etUsername.requestFocus()
            return false
        }

        if (email.isEmpty()) {
            etEmail.error = "Email harus diisi"
            etEmail.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Format email tidak valid"
            etEmail.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            etPassword.error = "Password harus diisi"
            etPassword.requestFocus()
            return false
        }

        if (confirm.isEmpty()) {
            etConfirm.error = "Konfirmasi password harus diisi"
            etConfirm.requestFocus()
            return false
        }

        if (password != confirm) {
            etConfirm.error = "Konfirmasi password tidak sama"
            etConfirm.requestFocus()
            return false
        }
        return true
    }

    private fun isValidPassword(password: String): Boolean = password.length >= 8
}