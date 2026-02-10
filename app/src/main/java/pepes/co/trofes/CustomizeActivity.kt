package pepes.co.trofes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch
import pepes.co.trofes.auth.BaseAuthActivity
import pepes.co.trofes.data.remote.RetrofitClient
import pepes.co.trofes.data.remote.extractAllergies
import pepes.co.trofes.data.remote.extractDietaryPreferences
import pepes.co.trofes.data.remote.extractIngredients

class CustomizeActivity : BaseAuthActivity() {

    private val openCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            val text = res.data?.getStringExtra(CameraActivity.EXTRA_RESULT_TEXT).orEmpty()
            if (text.isNotBlank()) {
                findViewById<android.widget.EditText?>(R.id.etIngredients)?.setText(text.trim())
            }
        }
    }

    // Allowed lists from API
    private var allowedAllergies: List<Pair<Long, String>> = emptyList() // id -> name
    private var allowedPreferences: List<Pair<Long, String>> = emptyList() // id -> name
    private var allowedIngredients: List<Pair<Long, String>> = emptyList() // id -> name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isAuthRedirected) return

        enableEdgeToEdge()
        setContentView(R.layout.activity_customize)

        // back
        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }

        // open camera -> OCR -> fill ingredients
        findViewById<View?>(R.id.btnCamera)?.setOnClickListener {
            openCamera.launch(Intent(this, CameraActivity::class.java))
        }

        val chipsAllergies = findViewById<ChipGroup>(R.id.chipsAllergies)
        val chipsPreferences = findViewById<ChipGroup>(R.id.chipsPreferences)
        val chipsIngredients = findViewById<ChipGroup>(R.id.chipsIngredients)

        val containerAllergies = findViewById<View>(R.id.containerAllergies)
        val containerPreferences = findViewById<View>(R.id.containerPreferences)
        val containerIngredients = findViewById<View>(R.id.containerIngredients)

        fun updateContainerBg(container: View, group: ChipGroup) {
            val hasChip = group.childCount > 0
            container.setBackgroundResource(
                if (hasChip) R.drawable.bg_input_border_active_gray else R.drawable.bg_input_border
            )
        }

        // Awal: kosong
        chipsAllergies.removeAllViews()
        chipsPreferences.removeAllViews()
        chipsIngredients.removeAllViews()
        updateContainerBg(containerAllergies, chipsAllergies)
        updateContainerBg(containerPreferences, chipsPreferences)
        updateContainerBg(containerIngredients, chipsIngredients)

        // Load master data dari API, lalu setup autocomplete
        lifecycleScope.launch {
            // Allergies
            launch {
                try {
                    val resp = RetrofitClient.apiService.getAllergies(page = 1, perPage = 200)
                    val list = resp.extractAllergies()
                    allowedAllergies = list.mapNotNull { a ->
                        val id = a.allergyId ?: a.id
                        val name = a.name?.trim()
                        if (id != null && !name.isNullOrBlank()) id to name else null
                    }
                } catch (_: Exception) {
                    // ignore -> tetap bisa manual kosong
                } finally {
                    setupAutoCompleteToChips(
                        input = findViewById(R.id.etAllergy),
                        chipGroup = chipsAllergies,
                        allowed = allowedAllergies,
                        emptyHint = getString(R.string.type_allergy),
                        onChipChanged = { updateContainerBg(containerAllergies, chipsAllergies) }
                    )
                }
            }

            // Preferences
            launch {
                try {
                    val resp = RetrofitClient.apiService.getDietaryPreferences(page = 1, perPage = 200)
                    val list = resp.extractDietaryPreferences()
                    allowedPreferences = list.mapNotNull { p ->
                        val id = p.dietaryPreferenceId ?: p.id
                        val name = (p.name ?: p.title)?.trim()
                        if (id != null && !name.isNullOrBlank()) id to name else null
                    }
                } catch (_: Exception) {
                    // ignore
                } finally {
                    setupAutoCompleteToChips(
                        input = findViewById(R.id.etPreferences),
                        chipGroup = chipsPreferences,
                        allowed = allowedPreferences,
                        emptyHint = getString(R.string.type_dietary_preference),
                        onChipChanged = { updateContainerBg(containerPreferences, chipsPreferences) }
                    )
                }
            }

            // Ingredients
            launch {
                try {
                    val resp = RetrofitClient.apiService.getIngredients(page = 1, perPage = 200)
                    val list = resp.extractIngredients()
                    allowedIngredients = list.mapNotNull { ing ->
                        val id = ing.ingredientId ?: ing.id
                        val name = ing.name?.trim()
                        if (id != null && !name.isNullOrBlank()) id to name else null
                    }
                } catch (_: Exception) {
                    // ignore
                } finally {
                    setupAutoCompleteToChips(
                        input = findViewById(R.id.etIngredientsAuto),
                        chipGroup = chipsIngredients,
                        allowed = allowedIngredients,
                        emptyHint = getString(R.string.type_ingredients),
                        onChipChanged = { updateContainerBg(containerIngredients, chipsIngredients) }
                    )
                }
            }
        }

        findViewById<TextView>(R.id.tvRemoveAllAllergies).setOnClickListener {
            chipsAllergies.removeAllViews()
            updateContainerBg(containerAllergies, chipsAllergies)
        }
        findViewById<TextView>(R.id.tvRemoveAllPreferences).setOnClickListener {
            chipsPreferences.removeAllViews()
            updateContainerBg(containerPreferences, chipsPreferences)
        }
        findViewById<TextView>(R.id.tvRemoveAllIngredients).setOnClickListener {
            chipsIngredients.removeAllViews()
            updateContainerBg(containerIngredients, chipsIngredients)
        }

        // A + B: tombol action -> buka RecipesActivity dengan filter
        findViewById<android.view.View>(R.id.btnSearchRecipes).setOnClickListener {
            // ingredient query: gabungkan chip ingredient jadi 1 string
            val ingredientQuery = (0 until chipsIngredients.childCount)
                .mapNotNull { i -> chipsIngredients.getChildAt(i) as? Chip }
                .joinToString(" ") { it.text.toString() }
                .trim()

            // fallback: free text ingredients input lama
            val freeText = findViewById<EditText?>(R.id.etIngredients)?.text?.toString().orEmpty().trim()

            val query = when {
                ingredientQuery.isNotBlank() -> ingredientQuery
                freeText.isNotBlank() -> freeText
                else -> ""
            }

            // pilih filter type/id berdasarkan chip yang dipilih
            val selectedDietId = (0 until chipsPreferences.childCount)
                .mapNotNull { i -> chipsPreferences.getChildAt(i) as? Chip }
                .firstOrNull()
                ?.tag as? Long

            val selectedAllergyId = (0 until chipsAllergies.childCount)
                .mapNotNull { i -> chipsAllergies.getChildAt(i) as? Chip }
                .firstOrNull()
                ?.tag as? Long

            val (filterType, filterId) = when {
                selectedDietId != null -> "diet" to selectedDietId.toInt()
                selectedAllergyId != null -> "no_allergy" to selectedAllergyId.toInt()
                else -> null to null
            }

            startActivity(
                RecipesActivity.newIntent(
                    context = this,
                    query = query.takeIf { it.isNotBlank() },
                    filterType = filterType,
                    filterId = filterId,
                )
            )
        }

        findViewById<android.view.View>(R.id.btnReset).setOnClickListener {
            // reset semua input & chips
            findViewById<android.widget.EditText?>(R.id.etCalories)?.setText("")
            findViewById<android.widget.EditText?>(R.id.etProtein)?.setText("")
            findViewById<android.widget.EditText?>(R.id.etFat)?.setText("")
            findViewById<android.widget.EditText?>(R.id.etCarbs)?.setText("")
            findViewById<android.widget.EditText?>(R.id.etSodium)?.setText("")
            findViewById<android.widget.EditText?>(R.id.etIngredients)?.setText("")

            findViewById<AutoCompleteTextView?>(R.id.etAllergy)?.setText("")
            findViewById<AutoCompleteTextView?>(R.id.etPreferences)?.setText("")
            findViewById<AutoCompleteTextView?>(R.id.etIngredientsAuto)?.setText("")

            chipsAllergies.removeAllViews()
            chipsPreferences.removeAllViews()
            chipsIngredients.removeAllViews()
            updateContainerBg(containerAllergies, chipsAllergies)
            updateContainerBg(containerPreferences, chipsPreferences)
            updateContainerBg(containerIngredients, chipsIngredients)

            Toast.makeText(this, "Reset clicked", Toast.LENGTH_SHORT).show()
        }
    }

    override fun requiredLoginIntent(): android.content.Intent = SigninIntentFactory.forCustomize(this)

    private fun setupAutoCompleteToChips(
        input: AutoCompleteTextView,
        chipGroup: ChipGroup,
        allowed: List<Pair<Long, String>>, // id -> label
        emptyHint: String,
        onChipChanged: () -> Unit
    ) {
        input.hint = emptyHint

        if (allowed.isEmpty()) {
            // Tetap bisa diketik, tapi tidak akan bisa jadi chip karena tidak ada daftar.
            input.setOnClickListener {
                Toast.makeText(this, "Daftar dari server belum tersedia", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val labels = allowed.map { it.second }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, labels)
        input.setAdapter(adapter)
        input.threshold = 1

        // Boleh diketik, dan saat klik tampilkan dropdown.
        input.setOnClickListener { input.showDropDown() }

        fun addChipIfNeeded(raw: String) {
            val typed = raw.trim()
            if (typed.isBlank()) return

            val canonical = allowed.firstOrNull { it.second.equals(typed, ignoreCase = true) }
            if (canonical == null) {
                Toast.makeText(this, "Harus pilih dari daftar", Toast.LENGTH_SHORT).show()
                return
            }

            val exists = (0 until chipGroup.childCount)
                .mapNotNull { chipGroup.getChildAt(it) as? Chip }
                .any { it.text.toString().equals(canonical.second, ignoreCase = true) }
            if (exists) {
                input.setText("")
                return
            }

            val chip = Chip(this).apply {
                text = canonical.second
                // simpan ID
                tag = canonical.first
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    chipGroup.removeView(this)
                    onChipChanged()
                }
            }
            chipGroup.addView(chip)
            input.setText("")
            onChipChanged()
        }

        input.setOnItemClickListener { parent, _, position, _ ->
            val selected = parent.getItemAtPosition(position)?.toString().orEmpty()
            addChipIfNeeded(selected)
        }

        // Done/Enter
        input.setOnEditorActionListener { _, actionId, event ->
            val isEnter = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP
            if (actionId == EditorInfo.IME_ACTION_DONE || isEnter) {
                addChipIfNeeded(input.text?.toString().orEmpty())
                true
            } else {
                false
            }
        }

        // Lose focus
        input.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val v = input.text?.toString().orEmpty().trim()
                if (v.isNotEmpty()) addChipIfNeeded(v)
            }
        }
    }
}
