package pepes.co.trofes

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import pepes.co.trofes.auth.AuthRepository
import pepes.co.trofes.auth.AuthSession

class SigninActivity : AppCompatActivity() {

    private var passwordVisible = false

    private lateinit var repo: AuthRepository

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

        repo = AuthRepository(this)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        val ivTogglePassword = findViewById<ImageView>(R.id.ivTogglePassword)
        ivTogglePassword.setOnClickListener {
            passwordVisible = !passwordVisible
            if (passwordVisible) {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivTogglePassword.setImageResource(R.drawable.ic_eye_on)
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivTogglePassword.setImageResource(R.drawable.ic_eye_off)
            }
            etPassword.setSelection(etPassword.text?.length ?: 0)
        }

        findViewById<TextView>(R.id.tvForgotPassword).setOnClickListener {
            Toast.makeText(this, "Fitur lupa password belum dibuat", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnGoogle).setOnClickListener {
            startGoogleSignIn()
        }

        findViewById<Button>(R.id.btnSignin).setOnClickListener {
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

            lifecycleScope.launch {
                try {
                    val res = repo.login(emailOrUsername, password)

                    if (res.success == true && res.data?.token != null && res.data.user != null) {
                        saveSessionAndRedirect(res.data.token, res.data.user)
                        return@launch
                    }

                    // Failed: tampilkan error sesuai API
                    val errors = res.errors
                    if (errors != null) {
                        // Login API biasanya kirim error di field 'login' atau 'password'
                        errors["login"]?.firstOrNull()?.let { etEmail.error = it }
                        errors["email"]?.firstOrNull()?.let { etEmail.error = it }
                        errors["username"]?.firstOrNull()?.let { etEmail.error = it }
                        errors["password"]?.firstOrNull()?.let { etPassword.error = it }

                        if (etEmail.error == null && etPassword.error == null) {
                            Toast.makeText(this@SigninActivity, res.message ?: "Login gagal", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@SigninActivity, res.message ?: "Login gagal", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@SigninActivity, "Login gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        findViewById<TextView>(R.id.tvSignupLink).setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun startGoogleSignIn() {
        // TODO: isi client id via local.properties/BuildConfig kalau diperlukan.
        // Untuk sementara: gunakan default requestIdToken (harus diisi WEB_CLIENT_ID agar dapat token).
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

        lifecycleScope.launch {
            try {
                val res = repo.googleLogin(idToken)
                if (res.success == true && res.data?.token != null && res.data.user != null) {
                    saveSessionAndRedirect(res.data.token, res.data.user)
                    return@launch
                }

                val googleErr = res.errors?.get("google")?.firstOrNull()
                Toast.makeText(this@SigninActivity, googleErr ?: res.message ?: "Google login gagal", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@SigninActivity, "Google login gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveSessionAndRedirect(token: String, user: pepes.co.trofes.data.remote.ApiUser) {
        repo.saveSession(
            token = token,
            userId = user.id ?: 0L,
            username = user.username.orEmpty(),
            email = user.email.orEmpty(),
            fullName = user.fullName.orEmpty().ifBlank { user.username.orEmpty() },
            profileImage = user.profileImage,
        )

        // redirect target
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
        }

        startActivity(Intent(this, HomeActivity::class.java))
        finishAffinity()
    }
}
