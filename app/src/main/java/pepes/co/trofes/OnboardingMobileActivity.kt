package pepes.co.trofes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class OnboardingMobileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_mobile)

        // Tile di layout ini sudah punya TextView label langsung, jadi tidak perlu set text via code.
        // Cukup toggle selected saat tile di-tap.
        val tileIds = intArrayOf(
            R.id.tileHalal,
            R.id.tileLactose,
            R.id.tileKeto,
            R.id.tileWeightLoss,
            R.id.tileHighProtein,
            R.id.tileGlutenFree,
            R.id.tileDairyFree,
            R.id.tileSpicy,
            R.id.tileNoFried
        )

        tileIds.forEach { tileId ->
            findViewById<android.view.View>(tileId).setOnClickListener { v ->
                v.isSelected = !v.isSelected
            }
        }

        fun saveSelectedPreferences() {
            // IntArray tidak selalu punya extension mapNotNull di beberapa konfigurasi; ubah ke List dulu
            val selected = tileIds.asList().mapNotNull { id ->
                val tile = findViewById<android.view.View>(id)
                if (!tile.isSelected) return@mapNotNull null

                val labelTv = (tile as? android.view.ViewGroup)
                    ?.let { group ->
                        // cari TextView pertama di dalam tile
                        (0 until group.childCount)
                            .map { group.getChildAt(it) }
                            .firstOrNull { it is android.widget.TextView } as? android.widget.TextView
                    }

                labelTv?.text?.toString()?.replace("\n", " ")?.trim()?.takeIf { it.isNotBlank() }
            }.distinct()

            getSharedPreferences("UserProfile", MODE_PRIVATE)
                .edit()
                .putString("onboarding_preferences", selected.joinToString("|"))
                .apply()
        }

        fun goToAllergies() {
            saveSelectedPreferences()
            startActivity(Intent(this, OnboardingAllergiesActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            goToAllergies()
        }

        findViewById<Button>(R.id.btnSkip).setOnClickListener {
            // Skip: tetap simpan kosong supaya Customize awalnya kosong
            saveSelectedPreferences()
            goToAllergies()
        }
    }
}
