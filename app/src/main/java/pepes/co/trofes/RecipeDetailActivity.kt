package pepes.co.trofes

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class RecipeDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_SUBTITLE = "extra_subtitle"
        const val EXTRA_RATING = "extra_rating"
        const val EXTRA_RATING_COUNT = "extra_rating_count"
        const val EXTRA_CALORIES = "extra_calories"
        const val EXTRA_TIME = "extra_time"
        const val EXTRA_TAGS = "extra_tags"
        const val EXTRA_SERVINGS = "extra_servings"
        const val EXTRA_IMAGE_RES = "extra_image_res"
        const val EXTRA_INGREDIENTS = "extra_ingredients"
        const val EXTRA_STEPS = "extra_steps"
        const val EXTRA_YOUTUBE_ID = "extra_youtube_id"
    }

    private lateinit var web: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recipe_detail)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty().ifEmpty { "Menu" }
        val subtitle = intent.getStringExtra(EXTRA_SUBTITLE).orEmpty()
        val rating = intent.getStringExtra(EXTRA_RATING).orEmpty().ifEmpty { "4.8" }
        val ratingCount = intent.getIntExtra(EXTRA_RATING_COUNT, 230)
        val calories = intent.getStringExtra(EXTRA_CALORIES).orEmpty().ifEmpty { "500kcal" }
        val time = intent.getStringExtra(EXTRA_TIME).orEmpty().ifEmpty { "36m" }
        val tags = intent.getStringExtra(EXTRA_TAGS).orEmpty().ifEmpty { "Halal" }
        val servings = intent.getStringExtra(EXTRA_SERVINGS).orEmpty().ifEmpty { "2" }
        val imageRes = intent.getIntExtra(EXTRA_IMAGE_RES, 0)
        val ingredientsText = intent.getStringExtra(EXTRA_INGREDIENTS).orEmpty()
        val steps = intent.getStringArrayListExtra(EXTRA_STEPS) ?: arrayListOf(
            "For the sachet, wrap the parsley, thyme, bay leaves, and peppercorns in cheesecloth.",
            "Preheat the oven to 325°F.",
            "Beginning with a large saucepan, heat oil over medium-high heat.",
        )

        val youtubeId = intent.getStringExtra(EXTRA_YOUTUBE_ID).orEmpty().ifEmpty { "dQw4w9WgXcQ" }

        findViewById<TextView>(R.id.tvTitle).text = title
        findViewById<TextView>(R.id.tvSubtitle).text = subtitle
        findViewById<TextView>(R.id.tvRating).text = rating
        findViewById<TextView>(R.id.tvRatingCount).text = "($ratingCount)"

        // Chips
        findViewById<TextView>(R.id.tvChipCalories).text = calories
        findViewById<TextView>(R.id.tvChipTime).text = time
        findViewById<TextView>(R.id.tvChipTags).text = tags
        findViewById<TextView>(R.id.tvChipServings).text = servings

        // Image
        val iv = findViewById<ImageView>(R.id.ivRecipe)
        if (imageRes != 0) iv.setImageResource(imageRes)

        // Ingredients
        findViewById<TextView>(R.id.tvIngredients).text = if (ingredientsText.isNotBlank()) {
            ingredientsText
        } else {
            "• Chicken\n• Miso\n• Butter\n• Parsley"
        }

        // Steps
        findViewById<TextView>(R.id.tvStep1).text = steps.getOrNull(0).orEmpty()
        findViewById<TextView>(R.id.tvStep2).text = steps.getOrNull(1).orEmpty()
        findViewById<TextView>(R.id.tvStep3).text = steps.getOrNull(2).orEmpty()

        web = findViewById(R.id.webYoutube)
        setupYoutube(web, youtubeId)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupYoutube(webView: WebView, youtubeId: String) {
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = WebViewClient()

        val s = webView.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.loadWithOverviewMode = true
        s.useWideViewPort = true
        s.mediaPlaybackRequiresUserGesture = true
        s.cacheMode = WebSettings.LOAD_DEFAULT

        val html = """
            <!DOCTYPE html>
            <html>
            <head>
              <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />
              <style>
                html, body { margin:0; padding:0; background:#000; height:100%; }
                .wrap { position:relative; width:100%; height:100%; }
                iframe { position:absolute; top:0; left:0; width:100%; height:100%; border:0; }
              </style>
            </head>
            <body>
              <div class=\"wrap\">
                <iframe
                  src=\"https://www.youtube.com/embed/$youtubeId\"
                  title=\"YouTube video\"
                  allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\"
                  allowfullscreen>
                </iframe>
              </div>
            </body>
            </html>
        """.trimIndent()

        webView.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "utf-8", null)
    }

    override fun onDestroy() {
        if (::web.isInitialized) {
            web.stopLoading()
            web.loadUrl("about:blank")
            web.clearHistory()
            web.removeAllViews()
            web.destroy()
        }
        super.onDestroy()
    }
}
