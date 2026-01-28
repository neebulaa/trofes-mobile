package pepes.co.trofes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import pepes.co.trofes.model.RecipeDetail
import pepes.co.trofes.ui.recipe.RecipeDetailScreen

class RecipeDetailComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val item = RecommendationItem(
            id = intent.getStringExtra(EXTRA_ID).orEmpty(),
            title = intent.getStringExtra(EXTRA_TITLE).orEmpty(),
            rating = intent.getStringExtra(EXTRA_RATING).orEmpty(),
            likesCount = intent.getIntExtra(EXTRA_LIKES, 230),
            caloriesText = intent.getStringExtra(EXTRA_CALORIES).orEmpty(),
            timeText = intent.getStringExtra(EXTRA_TIME).orEmpty(),
            tagText = intent.getStringExtra(EXTRA_TAG).orEmpty(),
            category = intent.getStringExtra(EXTRA_CATEGORY).orEmpty(),
            imageRes = intent.getIntExtra(EXTRA_IMAGE_RES, 0),
            isLiked = false,
        )

        val detail = RecipeDetail.fromRecommendation(item)

        setContent {
            RecipeDetailScreen(
                recipe = detail,
                onBack = { finish() },
            )
        }
    }

    companion object {
        private const val EXTRA_ID = "extra_id"
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_RATING = "extra_rating"
        private const val EXTRA_LIKES = "extra_likes"
        private const val EXTRA_CALORIES = "extra_calories"
        private const val EXTRA_TIME = "extra_time"
        private const val EXTRA_TAG = "extra_tag"
        private const val EXTRA_CATEGORY = "extra_category"
        private const val EXTRA_IMAGE_RES = "extra_image_res"

        fun newIntent(context: Context, item: RecommendationItem): Intent {
            return Intent(context, RecipeDetailComposeActivity::class.java).apply {
                putExtra(EXTRA_ID, item.id)
                putExtra(EXTRA_TITLE, item.title)
                putExtra(EXTRA_RATING, item.rating)
                putExtra(EXTRA_LIKES, item.likesCount)
                putExtra(EXTRA_CALORIES, item.caloriesText)
                putExtra(EXTRA_TIME, item.timeText)
                putExtra(EXTRA_TAG, item.tagText)
                putExtra(EXTRA_CATEGORY, item.category)
                putExtra(EXTRA_IMAGE_RES, item.imageRes)
            }
        }
    }
}
