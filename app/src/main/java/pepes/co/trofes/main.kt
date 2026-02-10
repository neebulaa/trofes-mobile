package pepes.co.trofes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pepes.co.trofes.auth.AuthSession

class MainActivity : AppCompatActivity() {

    private lateinit var authSession: AuthSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        authSession = AuthSession(this)

        // Force login first (sesuai request)
        if (!authSession.isLoggedIn()) {
            startActivity(SigninIntentFactory.forHome(this))
            finish()
            return
        }

        // Already logged in -> go straight to home
        startActivity(Intent(this, HomeActivity::class.java))
        finish()

        // (Fallback) kalau layout main masih dipakai
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnGetStarted = findViewById<Button?>(R.id.btnGetStarted)
        btnGetStarted?.setOnClickListener {
            if (!authSession.isLoggedIn()) {
                startActivity(SigninIntentFactory.forHome(this))
                finish()
                return@setOnClickListener
            }
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }
}