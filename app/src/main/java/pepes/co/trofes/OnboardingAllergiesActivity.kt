package pepes.co.trofes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pepes.co.trofes.data.remote.AllergiesApiResponse
import pepes.co.trofes.data.remote.RetrofitClient

class OnboardingAllergiesActivity : AppCompatActivity() {

    private val logTag = "OnboardingAllergies"

    // mapping tileId -> allergyId
    private val tileToAllergyId = mutableMapOf<Int, Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_allergies)

        // Multi select toggle untuk 9 tile
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

        // Load dari API untuk mengganti label
        loadAllergiesAndBind(tileIds)

        fun saveSelectedAllergies() {
            val selectedIds = tileIds.asList().mapNotNull { id ->
                val tile = findViewById<android.view.View>(id)
                if (!tile.isSelected) return@mapNotNull null
                tileToAllergyId[id]
            }.distinct()

            getSharedPreferences("UserProfile", MODE_PRIVATE)
                .edit()
                .putString("onboarding_allergy_ids", selectedIds.joinToString("|"))
                .apply()
        }

        fun goNext() {
            saveSelectedAllergies()
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        val btnContinue = findViewById<Button?>(R.id.btnContinue)
        val btnSkip = findViewById<Button?>(R.id.btnSkip)

        btnContinue?.setOnClickListener { goNext() }
        btnSkip?.setOnClickListener { goNext() }
    }

    private fun loadAllergiesAndBind(tileIds: IntArray) {
        lifecycleScope.launch {
            try {
                val resp: AllergiesApiResponse = RetrofitClient.apiService.getAllergies(page = 1, perPage = 9)
                val allergies = resp.data?.allergies?.data
                    ?: resp.data?.pageData
                    ?: emptyList()

                val firstNine = allergies.take(9)
                if (firstNine.isEmpty()) return@launch

                firstNine.forEachIndexed { index, allergy ->
                    if (index >= tileIds.size) return@forEachIndexed

                    val tileId = tileIds[index]
                    val tile = findViewById<android.view.View>(tileId)
                    val label = (allergy.name ?: "").trim()
                    val id = allergy.allergyId ?: allergy.id

                    if (id != null) tileToAllergyId[tileId] = id

                    if (label.isNotBlank()) {
                        val tv = (tile as? android.view.ViewGroup)
                            ?.let { group ->
                                (0 until group.childCount)
                                    .map { group.getChildAt(it) }
                                    .firstOrNull { it is TextView } as? TextView
                            }
                        tv?.text = label
                    }
                }
            } catch (e: Exception) {
                Log.e(logTag, "Failed load allergies: ${e.message}", e)
                Toast.makeText(this@OnboardingAllergiesActivity, "Gagal memuat allergies, pakai default", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
