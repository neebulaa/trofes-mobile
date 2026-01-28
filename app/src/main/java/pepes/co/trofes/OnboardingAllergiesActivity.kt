package pepes.co.trofes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class OnboardingAllergiesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_allergies)

        // Multi select toggle untuk 9 tile (nanti kamu ganti id/isi sesuai kebutuhan)
        val tileIds = intArrayOf(
            R.id.tile1,
            R.id.tile2,
            R.id.tile3,
            R.id.tile4,
            R.id.tile5,
            R.id.tile6,
            R.id.tile7,
            R.id.tile8,
            R.id.tile9
        )

        tileIds.forEach { tileId ->
            findViewById<android.view.View>(tileId).setOnClickListener { v ->
                v.isSelected = !v.isSelected
            }
        }

        fun saveSelectedAllergies() {
            // IntArray tidak selalu punya extension mapNotNull di beberapa konfigurasi; ubah ke List dulu
            val selected = tileIds.asList().mapNotNull { id ->
                val tile = findViewById<android.view.View>(id)
                if (!tile.isSelected) return@mapNotNull null

                val labelTv = (tile as? android.view.ViewGroup)
                    ?.let { group ->
                        (0 until group.childCount)
                            .map { group.getChildAt(it) }
                            .firstOrNull { it is android.widget.TextView } as? android.widget.TextView
                    }

                labelTv?.text?.toString()?.replace("\n", " ")?.trim()?.takeIf { it.isNotBlank() }
            }.distinct()

            getSharedPreferences("UserProfile", MODE_PRIVATE)
                .edit()
                .putString("onboarding_allergies", selected.joinToString("|"))
                .apply()
        }

        fun goNext() {
            saveSelectedAllergies()
            // lanjut ke Home untuk sekarang (sesuaikan jika flow berubah)
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        // Tombol di XML onboarding allergies sudah ada (btnContinue/btnSkip) di layout
        val btnContinue = findViewById<Button?>(R.id.btnContinue)
        val btnSkip = findViewById<Button?>(R.id.btnSkip)

        btnContinue?.setOnClickListener { goNext() }
        btnSkip?.setOnClickListener {
            saveSelectedAllergies()
            goNext()
        }
    }
}
