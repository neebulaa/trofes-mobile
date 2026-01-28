package pepes.co.trofes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class CustomizeActivity : AppCompatActivity() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customize)

        // back
        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }

        // open camera -> OCR -> fill ingredients
        findViewById<View?>(R.id.btnCamera)?.setOnClickListener {
            openCamera.launch(Intent(this, CameraActivity::class.java))
        }

        val prefs = getSharedPreferences("UserProfile", MODE_PRIVATE)
        val onboardingAllergies = prefs.getString("onboarding_allergies", "")
            .orEmpty()
            .split("|")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        val onboardingPreferences = prefs.getString("onboarding_preferences", "")
            .orEmpty()
            .split("|")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        val chipsAllergies = findViewById<ChipGroup>(R.id.chipsAllergies)
        val chipsPreferences = findViewById<ChipGroup>(R.id.chipsPreferences)

        val containerAllergies = findViewById<View>(R.id.containerAllergies)
        val containerPreferences = findViewById<View>(R.id.containerPreferences)

        fun updateContainerBg(container: View, group: ChipGroup) {
            val hasChip = group.childCount > 0
            container.setBackgroundResource(
                if (hasChip) R.drawable.bg_input_border_active_gray else R.drawable.bg_input_border
            )
        }

        // Awal: kosong
        chipsAllergies.removeAllViews()
        chipsPreferences.removeAllViews()
        updateContainerBg(containerAllergies, chipsAllergies)
        updateContainerBg(containerPreferences, chipsPreferences)

        // Setup autocomplete: allergies
        setupAutoCompleteToChips(
            input = findViewById(R.id.etAllergy),
            chipGroup = chipsAllergies,
            allowed = onboardingAllergies,
            emptyHint = getString(R.string.type_allergy),
            onChipChanged = { updateContainerBg(containerAllergies, chipsAllergies) }
        )

        // Setup autocomplete: preferences
        setupAutoCompleteToChips(
            input = findViewById(R.id.etPreferences),
            chipGroup = chipsPreferences,
            allowed = onboardingPreferences,
            emptyHint = getString(R.string.type_dietary_preference),
            onChipChanged = { updateContainerBg(containerPreferences, chipsPreferences) }
        )

        findViewById<TextView>(R.id.tvRemoveAllAllergies).setOnClickListener {
            chipsAllergies.removeAllViews()
            updateContainerBg(containerAllergies, chipsAllergies)
        }
        findViewById<TextView>(R.id.tvRemoveAllPreferences).setOnClickListener {
            chipsPreferences.removeAllViews()
            updateContainerBg(containerPreferences, chipsPreferences)
        }

        // tombol action (sementara placeholder)
        findViewById<android.view.View>(R.id.btnSearchRecipes).setOnClickListener {
            Toast.makeText(this, "Search Recipes clicked", Toast.LENGTH_SHORT).show()
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

            chipsAllergies.removeAllViews()
            chipsPreferences.removeAllViews()
            updateContainerBg(containerAllergies, chipsAllergies)
            updateContainerBg(containerPreferences, chipsPreferences)

            Toast.makeText(this, "Reset clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupAutoCompleteToChips(
        input: AutoCompleteTextView,
        chipGroup: ChipGroup,
        allowed: List<String>,
        emptyHint: String,
        onChipChanged: () -> Unit
    ) {
        input.hint = emptyHint

        if (allowed.isEmpty()) {
            // Tetap bisa diketik, tapi tidak akan bisa jadi chip karena tidak ada daftar.
            input.setOnClickListener {
                Toast.makeText(this, "Pilih dulu di onboarding agar ada daftar", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, allowed)
        input.setAdapter(adapter)
        input.threshold = 1

        // Boleh diketik, dan saat klik tampilkan dropdown.
        input.setOnClickListener { input.showDropDown() }

        fun addChipIfNeeded(raw: String) {
            val typed = raw.trim()
            if (typed.isBlank()) return

            val canonical = allowed.firstOrNull { it.equals(typed, ignoreCase = true) }
            if (canonical == null) {
                Toast.makeText(this, "Harus pilih dari daftar onboarding", Toast.LENGTH_SHORT).show()
                return
            }

            val exists = (0 until chipGroup.childCount)
                .mapNotNull { chipGroup.getChildAt(it) as? Chip }
                .any { it.text.toString().equals(canonical, ignoreCase = true) }
            if (exists) {
                input.setText("")
                return
            }

            val chip = Chip(this).apply {
                text = canonical
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
