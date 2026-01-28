package pepes.co.trofes

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar

class Onboarding_Isi_Profile : AppCompatActivity() {

    private lateinit var tvCountryCode: TextView
    private lateinit var spinnerGender: Spinner
    private lateinit var etBirthDate: EditText
    private lateinit var etFullName: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnLanjut: Button
    private lateinit var btnLewati: Button

    private var selectedGender: String? = null
    private var selectedCountry: Country = CountryUtils.defaultCountry()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_onboarding_isi_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Bind views (match current XML ids)
        tvCountryCode = findViewById(R.id.tvCountryCode)
        spinnerGender = findViewById(R.id.spGender)
        etBirthDate = findViewById(R.id.etBirthDate)
        etFullName = findViewById(R.id.etFullName)
        etPhone = findViewById(R.id.etPhone)
        btnLanjut = findViewById(R.id.btnNext)
        btnLewati = findViewById(R.id.btnSkip)

        setupCountryPicker()
        setupGenderSpinner()
        setupDatePicker()
        setupButtons()
    }

    private fun setupCountryPicker() {
        // default
        tvCountryCode.text = selectedCountry.dialCode

        tvCountryCode.setOnClickListener {
            val sheet = CountryPickerBottomSheet(selectedCountry) { picked ->
                selectedCountry = picked
                tvCountryCode.text = picked.dialCode
                // Optional UX: focus the phone input after selection
                etPhone.requestFocus()
            }
            sheet.show(supportFragmentManager, "country_picker")
        }
    }

    private fun setupGenderSpinner() {
        val genders = arrayOf("Jenis Kelamin", "Laki-laki", "Perempuan")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genders)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGender.adapter = adapter

        spinnerGender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedGender = if (position > 0) genders[position] else null
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedGender = null
            }
        }
    }

    private fun setupDatePicker() {
        etBirthDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    etBirthDate.setText(formattedDate)
                },
                year, month, day
            )
            datePickerDialog.show()
        }
    }

    private fun setupButtons() {
        fun goToOnboardingMobile() {
            try {
                val intent = Intent(this@Onboarding_Isi_Profile, OnboardingMobileActivity::class.java)
                startActivity(intent)
                // Optional: tutup halaman isi profile supaya back tidak balik lagi
                finish()
            } catch (e: Exception) {
                android.widget.Toast.makeText(
                    this@Onboarding_Isi_Profile,
                    "Gagal membuka onboarding mobile: ${'$'}{e.message}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }
        }

        btnLanjut.setOnClickListener {
            if (validateForm()) {
                saveProfileData()
                goToOnboardingMobile()
            }
        }

        btnLewati.setOnClickListener {
            goToOnboardingMobile()
        }
    }

    private fun validateForm(): Boolean {
        val fullName = etFullName.text.toString().trim()
        val phoneNumber = etPhone.text.toString().trim()
        val birthDate = etBirthDate.text.toString().trim()

        when {
            fullName.isEmpty() -> {
                etFullName.error = "Nama lengkap harus diisi"
                etFullName.requestFocus()
                return false
            }

            phoneNumber.isEmpty() -> {
                etPhone.error = "Nomor HP harus diisi"
                etPhone.requestFocus()
                return false
            }

            selectedGender == null -> {
                // Minimal: show error by forcing spinner focus
                spinnerGender.requestFocus()
                return false
            }

            birthDate.isEmpty() -> {
                etBirthDate.error = "Tanggal lahir harus diisi"
                etBirthDate.requestFocus()
                return false
            }
        }
        return true
    }

    private fun saveProfileData() {
        val fullName = etFullName.text.toString().trim()
        val countryCode = tvCountryCode.text.toString()
        val phoneNumber = etPhone.text.toString().trim()
        val gender = selectedGender
        val birthDate = etBirthDate.text.toString().trim()

        val sharedPref = getSharedPreferences("UserProfile", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("fullName", fullName)
            putString("phoneNumber", "$countryCode$phoneNumber")
            putString("gender", gender)
            putString("birthDate", birthDate)
            apply()
        }
    }
}