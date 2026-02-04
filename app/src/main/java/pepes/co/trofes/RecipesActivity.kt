package pepes.co.trofes

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import pepes.co.trofes.auth.AuthSession
import pepes.co.trofes.ui.CategoryChipsRow

class RecipesActivity : AppCompatActivity() {

    private lateinit var authSession: AuthSession

    private lateinit var adapter: RecommendationAdapter

    // Semua item yg sudah ter-load (infinite scroll menambah ke list ini)
    private val loadedItems = mutableListOf<RecommendationItem>()

    private var selectedCategory: String = "All Menu"

    // state paging
    private var isLoading = false
    private var page = 0
    private val pageSize = 20
    private val maxPages = 50 // total 1000 item dummy (20 * 50)

    private var currentQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recipes)

        authSession = AuthSession(this)

        setupTopActions()
        setupBottomNav()
        setupCategoryChipsCompose()
        setupGrid()
        setupSearch()

        syncHeaderAuthState()

        // load pertama
        loadNextPage()
    }

    override fun onResume() {
        super.onResume()
        syncHeaderAuthState()
    }

    private fun setupTopActions() {
        findViewById<MaterialButton?>(R.id.btnLogin)?.setOnClickListener {
            startActivity(SigninIntentFactory.forHome(this))
        }

        findViewById<ImageView?>(R.id.ivProfile)?.setOnClickListener {
            if (authSession.isLoggedIn()) {
                startActivity(Intent(this, ProfileActivity::class.java))
            } else {
                startActivity(SigninIntentFactory.forHome(this))
            }
        }
    }

    private fun syncHeaderAuthState() {
        val btnLogin = findViewById<MaterialButton?>(R.id.btnLogin)
        val ivProfile = findViewById<ImageView?>(R.id.ivProfile)

        val loggedIn = authSession.isLoggedIn()
        btnLogin?.visibility = if (loggedIn) View.GONE else View.VISIBLE
        ivProfile?.visibility = if (loggedIn) View.VISIBLE else View.GONE
    }

    private fun setupBottomNav() {
        findViewById<BottomNavigationView?>(R.id.bottomNavigation)?.apply {
            selectedItemId = R.id.nav_recipes

            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {
                        startActivity(Intent(this@RecipesActivity, HomeActivity::class.java))
                        finish()
                        true
                    }

                    R.id.nav_recipes -> true

                    R.id.nav_guide -> {
                        startActivity(Intent(this@RecipesActivity, GuideActivity::class.java))
                        finish()
                        true
                    }

                    R.id.nav_contact -> {
                        startActivity(Intent(this@RecipesActivity, ContactUsActivity::class.java))
                        finish()
                        true
                    }

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

    private fun setupCategoryChipsCompose() {
        val composeView = findViewById<ComposeView?>(R.id.categoryChipsCompose) ?: return
        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

        val main = listOf("All Menu", "Popular")
        val extra = listOf(
            "Halal",
            "Lactose Intolerance",
            "Keto / Low Carb",
            "Weight Loss",
            "High Protein",
            "Gluten Free",
            "Dairy Free",
            "Spicy",
            "No Fried Food",
            "Dairy",
            "Healty",
            "Vegan",
        )
        val categories = main + extra.shuffled()

        composeView.setContent {
            var selected by remember { mutableStateOf(selectedCategory) }
            CategoryChipsRow(
                categories = categories,
                selected = selected,
                onSelected = { newValue ->
                    selected = newValue
                    selectedCategory = newValue
                    applyCategoryFilterAndShow()
                },
            )
        }
    }

    private fun setupGrid() {
        val rv = findViewById<RecyclerView?>(R.id.rvRecipes) ?: return

        adapter = RecommendationAdapter(
            onItemClick = { item ->
                if (!authSession.isLoggedIn()) {
                    startActivity(SigninIntentFactory.forRecipeDetail(this, item))
                    return@RecommendationAdapter
                }
                startActivity(RecipeDetailComposeActivity.newIntent(this, item))
            },
            itemLayoutRes = R.layout.item_recommendation_grid,
        )

        val lm = GridLayoutManager(this, 2)
        rv.layoutManager = lm
        rv.adapter = adapter

        // Spacing grid supaya rapih (di baris/kolom)
        if (rv.itemDecorationCount == 0) {
            rv.addItemDecoration(
                pepes.co.trofes.GridSpacingItemDecoration(spanCount = 2, spacingDp = 14, includeEdge = false)
            )
        }

        // Infinite scroll
        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return

                val total = lm.itemCount
                val lastVisible = lm.findLastVisibleItemPosition()

                // load ketika mendekati bawah
                if (!isLoading && lastVisible >= total - 6) {
                    loadNextPage()
                }
            }
        })
    }

    private fun loadNextPage() {
        if (isLoading) return
        if (page >= maxPages) return

        isLoading = true
        // kalau mau: tampilkan loading state di masa depan

        val newItems = generateDummyPage(page, pageSize)
        page++

        loadedItems.addAll(newItems)
        applyFiltersAndShow()

        isLoading = false
    }

    private fun generateDummyPage(page: Int, size: Int): List<RecommendationItem> {
        val images = listOf(R.drawable.berita_1_, R.drawable.banner__1_)
        val categories = listOf(
            "Halal",
            "Lactose Intolerance",
            "Keto / Low Carb",
            "Weight Loss",
            "High Protein",
            "Gluten Free",
            "Dairy Free",
            "Spicy",
            "No Fried Food",
            "Dairy",
            "Healty",
            "Vegan",
        )
        val ratings = listOf("5.0", "4.0", "3.0", "2.5", "2.0", "1.0")

        val start = page * size
        return (0 until size).map { i ->
            val id = start + i
            val cat = categories[id % categories.size]
            val rating = ratings[id % ratings.size]
            val likes = 50 + (id * 13 % 1200)

            RecommendationItem(
                id = "r$id",
                title = "Menu #$id",
                rating = rating,
                likesCount = likes,
                caloriesText = (200 + (id % 300)).toString(),
                timeText = "${10 + (id % 40)}m",
                tagText = if (cat == "Halal") "Halal" else cat.split(" ").first(),
                category = cat,
                imageRes = images[id % images.size],
                firstDietaryPreference = (if (cat == "Halal") "Halal" else cat.split(" ").first()),
            )
        }
    }

    private fun setupSearch() {
        val etSearch = findViewById<EditText?>(R.id.etSearch) ?: return

        etSearch.setOnEditorActionListener { v, actionId, event ->
            val isSearchAction = actionId == EditorInfo.IME_ACTION_SEARCH
            val isEnter = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN

            if (isSearchAction || isEnter) {
                currentQuery = v.text?.toString().orEmpty()
                applyFiltersAndShow()
                true
            } else {
                false
            }
        }
    }

    private fun applyCategoryFilterAndShow() {
        applyFiltersAndShow()
    }

    private fun applyFiltersAndShow() {
        val byCategory = filterByCategory(loadedItems, selectedCategory)
        val q = currentQuery.trim()

        val finalList = if (q.isEmpty()) byCategory else byCategory.filter {
            it.title.contains(q, ignoreCase = true) ||
                it.tagText.contains(q, ignoreCase = true) ||
                it.category.contains(q, ignoreCase = true)
        }

        showList(finalList)
    }

    private fun filterByCategory(items: List<RecommendationItem>, category: String): List<RecommendationItem> {
        return when (category) {
            "All Menu" -> items
            "Popular" -> items.sortedByDescending { it.likesCount }.take(10)
            else -> items.filter { it.category.equals(category, ignoreCase = true) }
        }
    }

    private fun showList(items: List<RecommendationItem>) {
        val rv = findViewById<RecyclerView?>(R.id.rvRecipes)
        val empty = findViewById<LinearLayout?>(R.id.emptyRecipesState)
        val spacer = findViewById<View?>(R.id.bottomSpacer)

        adapter.submitList(items)

        val isEmpty = items.isEmpty()
        rv?.visibility = if (isEmpty) View.GONE else View.VISIBLE
        empty?.visibility = if (isEmpty) View.VISIBLE else View.GONE

        // Spacer mengikuti view yang terlihat supaya tinggi scroll enak
        spacer?.let {
            val params = it.layoutParams
            // jika list kosong, spacer tetap ada tapi tidak perlu terlalu tinggi
            params.height = if (isEmpty) resources.getDimensionPixelSize(R.dimen.recipes_bottom_spacer_empty) else resources.getDimensionPixelSize(R.dimen.recipes_bottom_spacer)
            it.layoutParams = params
        }
    }
}
