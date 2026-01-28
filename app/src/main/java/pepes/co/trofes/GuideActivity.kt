package pepes.co.trofes

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class GuideActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)

        setupBottomNav()

        val rv = findViewById<RecyclerView>(R.id.rvGuides)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = GuideAdapter(dummyGuides())
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
