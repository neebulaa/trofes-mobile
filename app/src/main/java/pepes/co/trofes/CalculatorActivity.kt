package pepes.co.trofes

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import pepes.co.trofes.auth.BaseAuthActivity

class CalculatorActivity : BaseAuthActivity() {

    private enum class Gender { MALE, FEMALE }

    private var selectedGender: Gender = Gender.MALE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isAuthRedirected) return

        enableEdgeToEdge()
        setContentView(R.layout.activity_calculator)

        setupBottomNav()
        setupForm()
    }

    private fun setupBottomNav() {
        findViewById<BottomNavigationView?>(R.id.bottomNavigation)?.apply {
            selectedItemId = R.id.nav_contact

            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {
                        startActivity(Intent(this@CalculatorActivity, HomeActivity::class.java))
                        finish()
                        true
                    }

                    R.id.nav_recipes -> {
                        startActivity(Intent(this@CalculatorActivity, RecipesActivity::class.java))
                        finish()
                        true
                    }

                    R.id.nav_guide -> {
                        startActivity(Intent(this@CalculatorActivity, GuideActivity::class.java))
                        finish()
                        true
                    }

                    R.id.nav_contact -> true

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

    private fun setupForm() {
        val btnMale = findViewById<MaterialButton>(R.id.btnMale)
        val btnFemale = findViewById<MaterialButton>(R.id.btnFemale)

        val etAge = findViewById<EditText>(R.id.etAge)
        val etWeight = findViewById<EditText>(R.id.etWeight)
        val etHeight = findViewById<EditText>(R.id.etHeight)

        val tvActivityLow = findViewById<TextView>(R.id.tvActivityLow)
        val tvActivityMiddle = findViewById<TextView>(R.id.tvActivityMiddle)
        val tvActivityHigh = findViewById<TextView>(R.id.tvActivityHigh)
        val tvActivityVeryHigh = findViewById<TextView>(R.id.tvActivityVeryHigh)

        val sbActivity = findViewById<SeekBar>(R.id.sbActivity)

        val btnCalculate = findViewById<MaterialButton>(R.id.btnCalculate)
        val btnReset = findViewById<MaterialButton>(R.id.btnReset)

        fun applyGenderUi() {
            val activeBg = getColor(R.color.trofes_green)
            val inactiveBg = getColor(R.color.trofes_grey_200)
            val white = getColor(android.R.color.white)
            val dark = getColor(R.color.trofes_text)

            val maleActive = selectedGender == Gender.MALE
            btnMale.setBackgroundColor(if (maleActive) activeBg else inactiveBg)
            btnMale.setTextColor(if (maleActive) white else dark)

            val femaleActive = selectedGender == Gender.FEMALE
            btnFemale.setBackgroundColor(if (femaleActive) activeBg else inactiveBg)
            btnFemale.setTextColor(if (femaleActive) white else dark)
        }

        btnMale.setOnClickListener {
            selectedGender = Gender.MALE
            applyGenderUi()
        }
        btnFemale.setOnClickListener {
            selectedGender = Gender.FEMALE
            applyGenderUi()
        }
        applyGenderUi()

        fun updateActivityLabels(level: Int) {
            fun setSelected(tv: TextView, selected: Boolean) {
                if (selected) {
                    tv.setBackgroundResource(R.drawable.bg_label_selected)
                    tv.setTextColor(getColor(android.R.color.white))
                } else {
                    tv.setBackgroundResource(0)
                    tv.setTextColor(getColor(R.color.trofes_text))
                }
            }

            setSelected(tvActivityLow, level == 0)
            setSelected(tvActivityMiddle, level == 1)
            setSelected(tvActivityHigh, level == 2)
            setSelected(tvActivityVeryHigh, level == 3)
        }

        fun setActivityLevel(level: Int) {
            val lv = level.coerceIn(0, 3)
            // update seekbar + label
            sbActivity.progress = lv
            updateActivityLabels(lv)
        }

        // klik label untuk memilih
        tvActivityLow.setOnClickListener { setActivityLevel(0) }
        tvActivityMiddle.setOnClickListener { setActivityLevel(1) }
        tvActivityHigh.setOnClickListener { setActivityLevel(2) }
        tvActivityVeryHigh.setOnClickListener { setActivityLevel(3) }

        sbActivity.max = 3
        setActivityLevel(2) // default ke High seperti gambar

        sbActivity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateActivityLabels(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val p = seekBar?.progress ?: return
                setActivityLevel(p)
            }
        })

        btnReset.setOnClickListener {
            selectedGender = Gender.MALE
            applyGenderUi()

            etAge.setText("")
            etWeight.setText("")
            etHeight.setText("")

            sbActivity.progress = 2
            updateActivityLabels(2)
        }

        // NOTE: sesuai permintaan, halaman ini fokus UI. Perhitungan bisa kita tambah setelah kamu fix rumusnya.
        btnCalculate.setOnClickListener {
            // Validasi ringan biar user paham input perlu diisi
            val age = etAge.text?.toString()?.trim()
            val weight = etWeight.text?.toString()?.trim()
            val height = etHeight.text?.toString()?.trim()

            val filled = !age.isNullOrBlank() && !weight.isNullOrBlank() && !height.isNullOrBlank()
            findViewById<View>(R.id.tvHintFillAll).visibility = if (filled) View.GONE else View.VISIBLE
        }
    }

    override fun requiredLoginIntent(): android.content.Intent = SigninIntentFactory.forCalculator(this)

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()
}
