package pepes.co.trofes

import android.content.Context
import android.content.Intent
import pepes.co.trofes.auth.AuthSession

/**
 * Helper untuk membuat intent ke SigninActivity sambil membawa data yang diperlukan
 * untuk redirect setelah login.
 */
object SigninIntentFactory {

    fun forHome(context: Context): Intent =
        Intent(context, SigninActivity::class.java).apply {
            putExtra(AuthSession.EXTRA_AFTER_LOGIN_TARGET, AuthSession.TARGET_HOME)
        }

    fun forRecipeDetail(context: Context, item: RecommendationItem): Intent {
        // Kita kirim extras yang sama seperti RecipeDetailComposeActivity.newIntent()
        return Intent(context, SigninActivity::class.java).apply {
            putExtra(AuthSession.EXTRA_AFTER_LOGIN_TARGET, AuthSession.TARGET_RECIPE_DETAIL)
            putExtras(RecipeDetailComposeActivity.newIntent(context, item).extras!!)
        }
    }
}
