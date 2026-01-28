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
import androidx.appcompat.app.AppCompatActivity

class SigninActivity : AppCompatActivity() {

    private var passwordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

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

        // Placeholder actions
        findViewById<TextView>(R.id.tvForgotPassword).setOnClickListener {
            Toast.makeText(this, "Fitur lupa password belum dibuat", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnGoogle).setOnClickListener {
            Toast.makeText(this, "Google Sign-In belum dibuat", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnSignin).setOnClickListener {
            val emailOrUsername = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            etEmail.error = null
            etPassword.error = null

            // Karena field bisa email atau username, validasi email hanya kalau mengandung '@'
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

            // Remember checkbox (placeholder: bisa disimpan ke SharedPreferences nanti)
            findViewById<CheckBox>(R.id.cbRemember).isChecked

            // TODO: implement autentikasi (Firebase/API) nanti
            // Untuk sekarang kita anggap berhasil
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        findViewById<TextView>(R.id.tvSignupLink).setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}
