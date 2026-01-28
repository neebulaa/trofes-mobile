package pepes.co.trofes

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SignupActivity : AppCompatActivity() {
    private var passwordVisible = false
    private var confirmPasswordVisible = false

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

        // Setup button listeners
        val btnSignup = findViewById<Button>(R.id.btnSignup)
        btnSignup.setOnClickListener {
            if (validateSignupForm()) {
                val intent = Intent(this, Onboarding_Isi_Profile::class.java)
                startActivity(intent)
                // optional: finish() // kalau mau signup tidak bisa dibuka lagi pakai tombol back
            }
        }

        val tvLoginLink = findViewById<TextView>(R.id.tvLoginLink)
        tvLoginLink.setOnClickListener {
            val intent = Intent(this, SigninActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Password visibility toggle
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val ivTogglePassword = findViewById<ImageView>(R.id.ivTogglePassword)

        val etConfirm = findViewById<EditText>(R.id.etConfirmPassword)
        val ivToggleConfirm = findViewById<ImageView>(R.id.ivToggleConfirm)

        ivTogglePassword.setOnClickListener {
            passwordVisible = !passwordVisible
            if (passwordVisible) {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivTogglePassword.setImageResource(R.drawable.ic_eye_on)
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivTogglePassword.setImageResource(R.drawable.ic_eye_off)
            }
            // Move cursor to end
            etPassword.setSelection(etPassword.text?.length ?: 0)
        }

        ivToggleConfirm.setOnClickListener {
            confirmPasswordVisible = !confirmPasswordVisible
            if (confirmPasswordVisible) {
                etConfirm.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivToggleConfirm.setImageResource(R.drawable.ic_eye_on)
            } else {
                etConfirm.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivToggleConfirm.setImageResource(R.drawable.ic_eye_off)
            }
            etConfirm.setSelection(etConfirm.text?.length ?: 0)
        }

    }

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

        if (!isValidPassword(password)) {
            etPassword.error = "Password harus 8-15 karakter dan wajib ada huruf kecil + angka"
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

    private fun isValidPassword(password: String): Boolean {
        // Panjang 8..15
        if (password.length !in 8..15) return false

        // Wajib ada huruf kecil dan angka
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }

        return hasLowercase && hasDigit
    }
}