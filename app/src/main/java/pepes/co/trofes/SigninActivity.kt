package pepes.co.trofes

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import pepes.co.trofes.auth.AuthRepositoryV1
import pepes.co.trofes.auth.AuthSession
import pepes.co.trofes.data.local.TokenManager
import pepes.co.trofes.data.remote.RetrofitClient
import pepes.co.trofes.ui.auth.LoginState
import pepes.co.trofes.ui.auth.LoginViewModel
import pepes.co.trofes.ui.auth.LoginViewModelFactory

class SigninActivity : AppCompatActivity() {

    private var passwordVisible = false

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSignin: Button
    private lateinit var btnGoogle: Button

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

    private val googleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            handleGoogleAccount(account)
        } catch (e: Exception) {
            Toast.makeText(this, "Google Sign-In gagal: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnSignin = findViewById(R.id.btnSignin)
        btnGoogle = findViewById(R.id.btnGoogle)

        val ivTogglePassword = findViewById<ImageView>(R.id.ivTogglePassword)
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

        findViewById<TextView>(R.id.tvForgotPassword).setOnClickListener {
            Toast.makeText(this, "Fitur lupa password belum dibuat", Toast.LENGTH_SHORT).show()
        }

        btnGoogle.setOnClickListener {
            startGoogleSignIn()
        }

        btnSignin.setOnClickListener {
            val emailOrUsername = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            etEmail.error = null
            etPassword.error = null

            if (emailOrUsername.isEmpty()) {
                etEmail.error = "Email/Username harus diisi"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (emailOrUsername.contains('@') && !Patterns.EMAIL_ADDRESS.matcher(emailOrUsername).matches()) {
                etEmail.error = "Format email tidak valid"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etPassword.error = "Password harus diisi"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            // Remember checkbox (placeholder)
            findViewById<CheckBox>(R.id.cbRemember).isChecked

            viewModel.login(emailOrUsername, password)
        }

        findViewById<TextView>(R.id.tvSignupLink).setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        observeLoginState()
    }

    private fun observeLoginState() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Loading -> {
                    btnSignin.isEnabled = false
                    btnGoogle.isEnabled = false
                }

                is LoginState.Success -> {
                    btnSignin.isEnabled = true
                    btnGoogle.isEnabled = true

                    Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show()
                    redirectAfterLogin(state.authResponse.user.onboardingCompleted)
                }

                is LoginState.Error -> {
                    btnSignin.isEnabled = true
                    btnGoogle.isEnabled = true

                    // Coba mapping error per-field kalau backend mengirim { errors: { login: [...], password: [...] } }
                    val applied = applyFieldErrorsIfAny(state.message)
                    if (!applied) {
                        Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    }
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

            val loginErr = firstError("login") ?: firstError("email") ?: firstError("username")
            val passErr = firstError("password")

            if (!loginErr.isNullOrBlank()) {
                etEmail.error = loginErr
                applied = true
            }
            if (!passErr.isNullOrBlank()) {
                etPassword.error = passErr
                applied = true
            }

            applied
        } catch (_: Exception) {
            false
        }
    }

    private fun redirectAfterLogin(onboardingCompleted: Boolean) {
        // Kalau onboarding belum selesai, arahkan ke onboarding flow.
        // Kalau belum ada activity onboarding yang pas, fallback ke Home.
        if (!onboardingCompleted) {
            // TODO: arahkan ke activity onboarding yang sesuai (sesuaikan dengan flow project kamu)
            startActivity(Intent(this, OnboardingMobileActivity::class.java))
            finishAffinity()
            return
        }

        // redirect target (tetap sama seperti implementasi lama)
        val target = intent.getStringExtra(AuthSession.EXTRA_AFTER_LOGIN_TARGET)
        when (target) {
            AuthSession.TARGET_RECIPE_DETAIL -> {
                startActivity(Intent(this, RecipeDetailComposeActivity::class.java).apply {
                    putExtras(intent.extras ?: Bundle())
                })
                finishAffinity()
                return
            }

            AuthSession.TARGET_GUIDE_DETAIL -> {
                startActivity(Intent(this, GuideDetailActivity::class.java).apply {
                    putExtras(intent.extras ?: Bundle())
                })
                finishAffinity()
                return
            }

            AuthSession.TARGET_RECIPES -> {
                startActivity(Intent(this, RecipesActivity::class.java))
                finishAffinity()
                return
            }

            AuthSession.TARGET_GUIDES -> {
                startActivity(Intent(this, GuideActivity::class.java))
                finishAffinity()
                return
            }

            AuthSession.TARGET_CUSTOMIZE -> {
                startActivity(Intent(this, CustomizeActivity::class.java))
                finishAffinity()
                return
            }

            AuthSession.TARGET_CALCULATOR -> {
                startActivity(Intent(this, CalculatorActivity::class.java))
                finishAffinity()
                return
            }

            AuthSession.TARGET_EDIT_PROFILE -> {
                startActivity(Intent(this, EditProfileActivity::class.java).apply {
                    putExtras(intent.extras ?: Bundle())
                })
                finishAffinity()
                return
            }

            AuthSession.TARGET_CONTACT_US -> {
                startActivity(Intent(this, ContactUsActivity::class.java))
                finishAffinity()
                return
            }

            AuthSession.TARGET_CHAT -> {
                startActivity(Intent(this, ChatActivity::class.java).apply {
                    putExtras(intent.extras ?: Bundle())
                })
                finishAffinity()
                return
            }

            AuthSession.TARGET_HOME -> {
                startActivity(Intent(this, HomeActivity::class.java))
                finishAffinity()
                return
            }
        }

        // fallback
        startActivity(Intent(this, HomeActivity::class.java))
        finishAffinity()
    }

    private fun startGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.default_web_client_id))
            .build()

        val client = GoogleSignIn.getClient(this, gso)
        googleLauncher.launch(client.signInIntent)
    }

    private fun handleGoogleAccount(account: GoogleSignInAccount?) {
        val idToken = account?.idToken
        if (idToken.isNullOrBlank()) {
            Toast.makeText(this, "Google ID token kosong. Pastikan default_web_client_id sudah benar.", Toast.LENGTH_LONG).show()
            return
        }

        viewModel.googleLogin(idToken)
    }
}
