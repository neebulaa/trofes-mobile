package pepes.co.trofes

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import pepes.co.trofes.auth.AuthSession

class GuideActivity : AppCompatActivity() {

    private lateinit var authSession: AuthSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)

        authSession = AuthSession(this)

        setupTopActions()
        setupBottomNav()

        val rv = findViewById<RecyclerView>(R.id.rvGuides)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = GuideAdapter(dummyGuides()) { guide ->
            if (!authSession.isLoggedIn()) {
                startActivity(
                    Intent(this, SigninActivity::class.java).apply {
                        putExtra(AuthSession.EXTRA_AFTER_LOGIN_TARGET, AuthSession.TARGET_GUIDE_DETAIL)
                        putExtras(GuideDetailActivity.newBundle(guide))
                    }
                )
                return@GuideAdapter
            }

            startActivity(Intent(this, GuideDetailActivity::class.java).apply {
                putExtras(GuideDetailActivity.newBundle(guide))
            })
        }

        syncHeaderAuthState()
    }

    override fun onResume() {
        super.onResume()
        // Kalau habis login / logout, header harus update
        syncHeaderAuthState()
    }

    private fun setupTopActions() {
        findViewById<MaterialButton?>(R.id.btnLogin)?.setOnClickListener {
            startActivity(SigninIntentFactory.forHome(this))
        }

        findViewById<ImageView?>(R.id.ivProfile)?.setOnClickListener {
            if (authSession.isLoggedIn()) {
                startActivity(Intent(this, ProfileActivity::class.java))
            } else {
                startActivity(SigninIntentFactory.forHome(this))
            }
        }
    }

    private fun syncHeaderAuthState() {
        val btnLogin = findViewById<MaterialButton?>(R.id.btnLogin)
        val ivProfile = findViewById<ImageView?>(R.id.ivProfile)

        val loggedIn = authSession.isLoggedIn()
        btnLogin?.visibility = if (loggedIn) View.GONE else View.VISIBLE
        ivProfile?.visibility = if (loggedIn) View.VISIBLE else View.GONE
    }

    private fun setupBottomNav() {
        findViewById<BottomNavigationView?>(R.id.bottomNavigation)?.apply {
            selectedItemId = R.id.nav_guide

            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {
                        startActivity(Intent(this@GuideActivity, HomeActivity::class.java))
                        finish()
                        true
                    }

                    R.id.nav_recipes -> {
                        startActivity(Intent(this@GuideActivity, RecipesActivity::class.java))
                        finish()
                        true
                    }

                    R.id.nav_guide -> true

                    R.id.nav_contact -> {
                        startActivity(Intent(this@GuideActivity, ContactUsActivity::class.java))
                        finish()
                        true
                    }

                    R.id.nav_placeholder -> false

                    else -> false
                }
            }
        }

        // Center FAB = Customize
        findViewById<FloatingActionButton?>(R.id.fabCenter)?.setOnClickListener {
            startActivity(Intent(this, CustomizeActivity::class.java))
        }
    }

    private fun dummyGuides(): List<GuideArticle> = listOf(
        GuideArticle(
            title = "Tentang Vitamin",
            desc = "Vitamin adalah senyawa penting yang dibutuhkan tubuh dalam jumlah kecil ...",
            date = "18/10/25",
            imageRes = R.drawable.guide_img_1
        ),
        GuideArticle(
            title = "Tips Memasak Sehat",
            desc = "Pelajari cara memasak sehat tanpa kehilangan rasa dan nutrisi ...",
            date = "18/10/25",
            imageRes = R.drawable.guide_img_2
        ),
        GuideArticle(
            title = "Panduan Diet Keto",
            desc = "Diet keto bisa membantu menurunkan berat badan dengan cepat ...",
            date = "18/10/25",
            imageRes = R.drawable.guide_img_3
        )
    )
}
