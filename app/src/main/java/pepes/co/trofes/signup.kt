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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pepes.co.trofes.auth.AuthRepository

class SignupActivity : AppCompatActivity() {
    private var passwordVisible = false
    private var confirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)

        val repo = AuthRepository(this)

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirm = findViewById<EditText>(R.id.etConfirmPassword)

        // Setup button listeners
        val btnSignup = findViewById<Button>(R.id.btnSignup)
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

            lifecycleScope.launch {
                try {
                    val res = repo.register(username, email, password, confirm)

                    if (res.success == true && res.data?.token != null && res.data.user != null) {
                        val user = res.data.user
                        // auto-login: simpan session
                        repo.saveSession(
                            token = res.data.token,
                            userId = user.id ?: 0L,
                            username = user.username.orEmpty(),
                            email = user.email.orEmpty(),
                            fullName = user.fullName.orEmpty().ifBlank { user.username.orEmpty() },
                            profileImage = user.profileImage,
                        )

                        // register berhasil -> onboarding page
                        startActivity(Intent(this@SignupActivity, Onboarding_Isi_Profile::class.java))
                        finishAffinity()
                        return@launch
                    }

                    val errors = res.errors
                    if (errors != null) {
                        errors["username"]?.firstOrNull()?.let { etUsername.error = it }
                        errors["email"]?.firstOrNull()?.let { etEmail.error = it }
                        errors["password"]?.firstOrNull()?.let { etPassword.error = it }
                        errors["password_confirmation"]?.firstOrNull()?.let { etConfirm.error = it }

                        if (etUsername.error == null && etEmail.error == null && etPassword.error == null && etConfirm.error == null) {
                            Toast.makeText(this@SignupActivity, res.message ?: "Register gagal", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@SignupActivity, res.message ?: "Register gagal", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@SignupActivity, "Register gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val tvLoginLink = findViewById<TextView>(R.id.tvLoginLink)
        tvLoginLink.setOnClickListener {
            val intent = Intent(this, SigninActivity::class.java)
            startActivity(intent)
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