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

    fun forGuideDetail(context: Context, guide: GuideArticle): Intent {
        return Intent(context, SigninActivity::class.java).apply {
            putExtra(AuthSession.EXTRA_AFTER_LOGIN_TARGET, AuthSession.TARGET_GUIDE_DETAIL)
            putExtras(GuideDetailActivity.newBundle(guide))
        }
    }

    fun forRecipes(context: Context): Intent =
        Intent(context, SigninActivity::class.java).apply {
            putExtra(AuthSession.EXTRA_AFTER_LOGIN_TARGET, AuthSession.TARGET_RECIPES)
        }

    fun forGuides(context: Context): Intent =
        Intent(context, SigninActivity::class.java).apply {
            putExtra(AuthSession.EXTRA_AFTER_LOGIN_TARGET, AuthSession.TARGET_GUIDES)
        }

    fun forCustomize(context: Context): Intent =
        Intent(context, SigninActivity::class.java).apply {
            putExtra(AuthSession.EXTRA_AFTER_LOGIN_TARGET, AuthSession.TARGET_CUSTOMIZE)
        }

    fun forCalculator(context: Context): Intent =
        Intent(context, SigninActivity::class.java).apply {
            putExtra(AuthSession.EXTRA_AFTER_LOGIN_TARGET, AuthSession.TARGET_CALCULATOR)
        }

    fun forEditProfile(context: Context): Intent =
        Intent(context, SigninActivity::class.java).apply {
            putExtra(AuthSession.EXTRA_AFTER_LOGIN_TARGET, AuthSession.TARGET_EDIT_PROFILE)
        }

    fun forContactUs(context: Context): Intent =
        Intent(context, SigninActivity::class.java).apply {
            putExtra(AuthSession.EXTRA_AFTER_LOGIN_TARGET, AuthSession.TARGET_CONTACT_US)
        }

    fun forChat(context: Context): Intent =
        Intent(context, SigninActivity::class.java).apply {
            putExtra(AuthSession.EXTRA_AFTER_LOGIN_TARGET, AuthSession.TARGET_CHAT)
        }
}
