package pepes.co.trofes

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import pepes.co.trofes.auth.BaseAuthActivity

class ProfileActivity : BaseAuthActivity() {

    private lateinit var prefs: ProfilePrefs

    private lateinit var tvName: TextView
    private lateinit var tvBio: TextView
    private lateinit var tvFullNameValue: TextView
    private lateinit var tvEmailValue: TextView
    private lateinit var tvPhoneValue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isAuthRedirected) return

        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        prefs = ProfilePrefs(this)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvEdit).setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            prefs.clear()
            startActivity(Intent(this, SigninActivity::class.java))
            finishAffinity()
        }

        tvName = findViewById(R.id.tvName)
        tvBio = findViewById(R.id.tvBio)
        tvFullNameValue = findViewById(R.id.tvNameValue)
        tvEmailValue = findViewById(R.id.tvEmailValue)
        tvPhoneValue = findViewById(R.id.tvPhoneValue)

        // Liked recipes grid (dummy supaya UI mirip contoh)
        val rvLiked = findViewById<RecyclerView>(R.id.rvLiked)
        rvLiked.layoutManager = GridLayoutManager(this, 2)
        rvLiked.isNestedScrollingEnabled = false

        val likedItems = (1..4).map { i ->
            RecommendationItem(
                id = "liked$i",
                title = "Beans Frizzled",
                rating = "4.8",
                likesCount = 500,
                caloriesText = "500",
                timeText = "",
                tagText = "Halal",
                category = "Popular",
                imageRes = R.drawable.sample_food_1,
                isLiked = true,
                firstDietaryPreference = "All",
            )

        }

        val adapter = RecommendationAdapter(
            onItemClick = { /* optional */ },
            itemLayoutRes = R.layout.item_recommendation_grid,
        )
        rvLiked.adapter = adapter
        adapter.submitList(likedItems)

        if (rvLiked.itemDecorationCount == 0) {
            rvLiked.addItemDecoration(GridSpacingItemDecoration(spanCount = 2, spacingDp = 14, includeEdge = false))
        }
    }

    override fun onResume() {
        super.onResume()
        bindProfile()
    }

    private fun bindProfile() {
        val p = prefs.getProfile()

        tvName.text = p.fullName.ifBlank { "Your Name" }
        tvBio.text = p.bio.ifBlank { "" }

        tvFullNameValue.text = p.fullName.ifBlank { "-" }
        tvEmailValue.text = p.email.ifBlank { "-" }
        tvPhoneValue.text = p.phone.ifBlank { "-" }
    }
}
