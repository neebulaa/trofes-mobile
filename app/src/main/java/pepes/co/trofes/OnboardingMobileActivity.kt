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
import pepes.co.trofes.data.remote.DietaryPreferencesApiResponse
import pepes.co.trofes.data.remote.RetrofitClient

class OnboardingMobileActivity : AppCompatActivity() {

    private val logTag = "OnboardingPrefs"

    // mapping tileId -> dietaryPreferenceId
    private val tileToPrefId = mutableMapOf<Int, Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_mobile)

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

        // toggle selected saat tile di-tap.
        tileIds.forEach { tileId ->
            findViewById<android.view.View>(tileId).setOnClickListener { v ->
                v.isSelected = !v.isSelected
            }
        }

        // Load dari API untuk mengganti label
        loadDietaryPreferencesAndBind(tileIds)

        fun saveSelectedPreferences() {
            val selectedIds = tileIds.asList().mapNotNull { id ->
                val tile = findViewById<android.view.View>(id)
                if (!tile.isSelected) return@mapNotNull null
                tileToPrefId[id]
            }.distinct()

            getSharedPreferences("UserProfile", MODE_PRIVATE)
                .edit()
                .putString("onboarding_preference_ids", selectedIds.joinToString("|"))
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
            saveSelectedPreferences()
            goToAllergies()
        }
    }

    private fun loadDietaryPreferencesAndBind(tileIds: IntArray) {
        lifecycleScope.launch {
            try {
                val resp: DietaryPreferencesApiResponse = RetrofitClient.apiService.getDietaryPreferences(page = 1, perPage = 9)
                val prefs = resp.data?.dietaryPreferences?.data
                    ?: resp.data?.pageData
                    ?: emptyList()

                val firstNine = prefs.take(9)
                if (firstNine.isEmpty()) return@launch

                // Update UI label sesuai urutan data
                firstNine.forEachIndexed { index, pref ->
                    if (index >= tileIds.size) return@forEachIndexed

                    val tileId = tileIds[index]
                    val tile = findViewById<android.view.View>(tileId)
                    val label = (pref.name ?: pref.title ?: "").trim()
                    val id = pref.dietaryPreferenceId ?: pref.id

                    if (id != null) tileToPrefId[tileId] = id

                    // ganti teks TextView pertama di dalam tile
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
                Log.e(logTag, "Failed load dietary preferences: ${e.message}", e)
                Toast.makeText(this@OnboardingMobileActivity, "Gagal memuat preferensi, pakai default", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
