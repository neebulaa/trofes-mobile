package pepes.co.trofes

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import pepes.co.trofes.auth.BaseAuthActivity

class EditProfileActivity : BaseAuthActivity() {

    private lateinit var prefs: ProfilePrefs

    private lateinit var etFullName: EditText
    private lateinit var etUsername: EditText
    private lateinit var etBio: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etBirthDate: EditText

    private lateinit var ddGender: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isAuthRedirected) return

        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        prefs = ProfilePrefs(this)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener { finish() }

        etFullName = findViewById(R.id.etFullName)
        etUsername = findViewById(R.id.etUsername)
        etBio = findViewById(R.id.etBio)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etBirthDate = findViewById(R.id.etBirthDate)
        ddGender = findViewById(R.id.ddGender)

        // Dropdown gender
        val genderItems = listOf("Laki-laki", "Perempuan", "Tidak ingin menyebutkan")
        (ddGender as? android.widget.AutoCompleteTextView)?.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, genderItems)
        )

        val current = prefs.getProfile()
        etFullName.setText(current.fullName)
        etUsername.setText(current.username)
        (ddGender as? android.widget.AutoCompleteTextView)?.setText(current.gender, false)
        etBio.setText(current.bio)
        etEmail.setText(current.email)
        etPhone.setText(current.phone)
        etBirthDate.setText(current.birthDate)

        // Preview header
        findViewById<TextView>(R.id.tvName).text = current.fullName.ifBlank { "Your Name" }
        findViewById<TextView>(R.id.tvBioPreview).text = current.bio.ifBlank { "" }

        etBirthDate.setOnClickListener { openDatePicker() }

        findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            val updated = UserProfile(
                fullName = etFullName.text?.toString().orEmpty().trim(),
                username = etUsername.text?.toString().orEmpty().trim(),
                gender = ddGender.text?.toString().orEmpty().trim(),
                bio = etBio.text?.toString().orEmpty().trim(),
                email = etEmail.text?.toString().orEmpty().trim(),
                phone = etPhone.text?.toString().orEmpty().trim(),
                birthDate = etBirthDate.text?.toString().orEmpty().trim(),
            )
            prefs.saveProfile(updated)
            finish()
        }
    }

    private fun openDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, y, m, d ->
                val picked = Calendar.getInstance().apply { set(y, m, d) }
                val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                etBirthDate.setText(fmt.format(picked.time))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun requiredLoginIntent(): android.content.Intent = SigninIntentFactory.forEditProfile(this)
}
