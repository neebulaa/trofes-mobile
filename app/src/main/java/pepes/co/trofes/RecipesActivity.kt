package pepes.co.trofes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import pepes.co.trofes.auth.BaseAuthActivity
import pepes.co.trofes.data.remote.RetrofitClient
import pepes.co.trofes.data.remote.extractDietaryPreferences
import pepes.co.trofes.data.remote.extractRecipes
import pepes.co.trofes.data.remote.lastPage
import pepes.co.trofes.data.remote.nextPageUrl
import pepes.co.trofes.ui.CategoryChipsRow

class RecipesActivity : BaseAuthActivity() {

    override fun requiredLoginIntent(): android.content.Intent = SigninIntentFactory.forRecipes(this)

    private lateinit var adapter: RecommendationAdapter
    private lateinit var gridLayoutManager: GridLayoutManager

    // Semua item yg sudah ter-load dari API
    private val loadedItems = mutableListOf<RecommendationItem>()

    private var selectedCategory: String = "All Menu"

    private var isLoading = false
    private var currentQuery: String = ""

    // paging
    private var currentPage = 1
    private var canLoadMore = true
    private val perPage = 16

    // filter backend
    private var selectedFilterType: String? = null
    private var selectedFilterId: Int? = null

    private lateinit var pbLoadMore: ProgressBar
    private lateinit var tvLoadMore: TextView
    private lateinit var tvNoMoreRecipes: TextView

    private val logTag = "RecipesPaging"

    private data class FilterSpec(val type: String?, val id: Int?)
    private val chipFilterMap: MutableMap<String, FilterSpec> = linkedMapOf()
    private var dynamicCategories: List<String> = listOf("All Menu", "Popular")

    companion object {
        const val EXTRA_QUERY = "extra_query"
        const val EXTRA_FILTER_TYPE = "extra_filter_type"
        const val EXTRA_FILTER_ID = "extra_filter_id"

        fun newIntent(
            context: android.content.Context,
            query: String? = null,
            filterType: String? = null,
            filterId: Int? = null,
        ): Intent {
            return Intent(context, RecipesActivity::class.java).apply {
                putExtra(EXTRA_QUERY, query)
                putExtra(EXTRA_FILTER_TYPE, filterType)
                if (filterId != null) putExtra(EXTRA_FILTER_ID, filterId)
            }
        }
    }

    // helper priority supaya konsisten
    private fun applyFilterPriority(
        dietId: Int?,
        ingredientId: Int?,
        noAllergyId: Int?,
        popular: Boolean,
    ) {
        when {
            dietId != null -> {
                selectedFilterType = "diet"
                selectedFilterId = dietId
            }

            ingredientId != null -> {
                selectedFilterType = "ingredient"
                selectedFilterId = ingredientId
            }

            noAllergyId != null -> {
                selectedFilterType = "no_allergy"
                selectedFilterId = noAllergyId
            }

            popular -> {
                selectedFilterType = "popular"
                selectedFilterId = null
            }

            else -> {
                selectedFilterType = null
                selectedFilterId = null
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isAuthRedirected) return

        enableEdgeToEdge()
        setContentView(R.layout.activity_recipes)

        pbLoadMore = findViewById(R.id.pbLoadMore)
        tvLoadMore = findViewById(R.id.tvLoadMore)
        tvNoMoreRecipes = findViewById(R.id.tvNoMoreRecipes)

        setupTopActions()
        setupBottomNav()
        setupGrid()
        setupSearch()

        // Load dynamic chips from backend then set compose chips
        loadChipsFromBackendAndSetupCompose()

        // apply incoming filters from Customize
        currentQuery = intent.getStringExtra(EXTRA_QUERY).orEmpty()

        // incoming: hanya satu filterType/Id diintent, tapi kita tetap lewat helper supaya future-proof
        val incomingType = intent.getStringExtra(EXTRA_FILTER_TYPE)
        val incomingId = intent.extras?.let {
            if (it.containsKey(EXTRA_FILTER_ID)) it.getInt(EXTRA_FILTER_ID) else null
        }

        applyFilterPriority(
            dietId = if (incomingType == "diet") incomingId else null,
            ingredientId = if (incomingType == "ingredient") incomingId else null,
            noAllergyId = if (incomingType == "no_allergy") incomingId else null,
            popular = incomingType == "popular",
        )

        syncHeaderAuthState()

        // load page pertama
        resetAndLoadFirstPage()
    }

    override fun onResume() {
        super.onResume()
        syncHeaderAuthState()
    }

    private fun resetAndLoadFirstPage() {
        loadedItems.clear()
        adapter.submitList(emptyList())

        // reset footer
        pbLoadMore.visibility = View.GONE
        tvLoadMore.visibility = View.GONE
        tvNoMoreRecipes.visibility = View.GONE

        currentPage = 1
        canLoadMore = true

        fetchRecipesPage(page = currentPage, query = currentQuery)
    }

    private fun fetchRecipesPage(page: Int, query: String) {
        if (isLoading) return
        if (!canLoadMore) return

        Log.d(logTag, "fetchRecipesPage(page=$page, perPage=$perPage, query='${query}', filterType=$selectedFilterType, filterId=$selectedFilterId)")

        isLoading = true

        // footer state
        tvNoMoreRecipes.visibility = View.GONE
        val showFooterLoading = page > 1
        pbLoadMore.visibility = if (showFooterLoading) View.VISIBLE else View.GONE
        tvLoadMore.visibility = if (showFooterLoading) View.VISIBLE else View.GONE

        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.apiService.getRecipes(
                    page = page,
                    perPage = perPage,
                    search = query.takeIf { it.isNotBlank() },
                    filterType = selectedFilterType,
                    filterId = selectedFilterId,
                )

                val recipes = resp.extractRecipes()

                // update canLoadMore dari meta pagination (kalau ada)
                val last = resp.lastPage()
                canLoadMore = when {
                    !resp.nextPageUrl().isNullOrBlank() -> true
                    last != null -> page < last
                    else -> recipes.isNotEmpty()
                }

                val mapped = recipes.map { r ->
                    val id = (r.recipeId ?: r.id ?: 0L).toString()
                    val title = r.title.orEmpty()

                    val ratingStr = (r.rating ?: 0.0).toString()
                    val likes = r.likesCount ?: 0

                    val cookingTime = r.cookingTime ?: 0
                    val totalIngredient = r.totalIngredient ?: 0
                    val imageUrl = r.publicImage ?: r.image
                    val isLiked = r.likedByMe ?: r.isLiked ?: false

                    // Label chip hijau = dietary preference pertama dari recipe (sesuai backend).
                    // Kalau kosong, biarkan kosong (UI bisa hide/placeholder).
                    val firstDiet = r.dietaryPreferences
                        .firstOrNull()
                        ?.let { it.name ?: it.title }
                        ?.trim()
                        ?.takeIf { it.isNotBlank() }

                    val tagText = firstDiet.orEmpty()

                    RecommendationItem(
                        id = id,
                        title = title,
                        rating = ratingStr,
                        likesCount = likes,
                        caloriesText = totalIngredient.toString(),
                        timeText = if (cookingTime > 0) "${cookingTime}m" else "-",
                        tagText = tagText,
                        category = "All",
                        ingredientsCount = totalIngredient,
                        imageUrl = imageUrl,
                        isLiked = isLiked,
                        firstDietaryPreference = tagText,
                    )
                }

                if (page == 1) {
                    loadedItems.clear()
                }

                loadedItems.addAll(mapped)
                applyFiltersAndShow()

                // update page counter
                currentPage = page

                // safety: kalau server return empty list, jangan load lagi
                if (recipes.isEmpty()) {
                    canLoadMore = false
                }

                // tampilkan "No more" kalau sudah habis dan list ada isinya
                tvNoMoreRecipes.visibility = if (!canLoadMore && loadedItems.isNotEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                Log.e(logTag, "fetch error page=$page: ${e.message}", e)
                Toast.makeText(this@RecipesActivity, "Gagal memuat recipes: ${e.message}", Toast.LENGTH_SHORT).show()

                // kalau page pertama gagal, tampilkan empty
                if (page == 1) {
                    loadedItems.clear()
                    applyFiltersAndShow()
                }
            } finally {
                isLoading = false
                pbLoadMore.visibility = View.GONE
                tvLoadMore.visibility = View.GONE

                // pastikan no-more tetap benar
                tvNoMoreRecipes.visibility = if (!canLoadMore && loadedItems.isNotEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun loadNextPageIfNeeded() {
        if (isLoading || !canLoadMore) return
        fetchRecipesPage(page = currentPage + 1, query = currentQuery)
    }

    private fun setupTopActions() {
        findViewById<MaterialButton?>(R.id.btnLogin)?.setOnClickListener {
            startActivity(SigninIntentFactory.forRecipes(this))
        }

        findViewById<ImageView?>(R.id.ivProfile)?.setOnClickListener {
            if (authSession.isLoggedIn()) {
                startActivity(Intent(this, ProfileActivity::class.java))
            } else {
                startActivity(SigninIntentFactory.forRecipes(this))
            }
        }
    }

    private fun syncHeaderAuthState() {
        val btnLogin = findViewById<MaterialButton?>(R.id.btnLogin)
        val ivProfile = findViewById<ImageView?>(R.id.ivProfile)
        val tvGreeting = findViewById<TextView?>(R.id.tvGreeting)

        val loggedIn = authSession.isLoggedIn()
        btnLogin?.visibility = if (loggedIn) View.GONE else View.VISIBLE
        ivProfile?.visibility = if (loggedIn) View.VISIBLE else View.GONE

        if (loggedIn) {
            val username = authSession.getUser()?.username?.ifBlank { "" }.orEmpty()
            tvGreeting?.text = if (username.isNotBlank()) "Hi $username" else "Hi"
        } else {
            tvGreeting?.text = "Hi"
        }
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
                        startActivity(Intent(this@RecipesActivity, CalculatorActivity::class.java))
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

    private fun loadChipsFromBackendAndSetupCompose() {
        lifecycleScope.launch {
            // default dulu biar UI tidak kosong
            buildDefaultChipFilters()
            setupCategoryChipsCompose(dynamicCategories)

            try {
                val dietsResp = RetrofitClient.apiService.getDietaryPreferences(page = 1, perPage = 200)
                val dietsList = dietsResp.extractDietaryPreferences()

                chipFilterMap.clear()
                chipFilterMap["All Menu"] = FilterSpec(null, null)
                chipFilterMap["Popular"] = FilterSpec("popular", null)

                dietsList
                    .mapNotNull { d ->
                        val id = (d.dietaryPreferenceId ?: d.id)?.toInt() ?: return@mapNotNull null
                        val name = (d.name ?: d.title)?.trim().orEmpty()
                        if (name.isBlank()) return@mapNotNull null
                        name to id
                    }
                    .distinctBy { it.second }
                    .forEach { (name, id) ->
                        // label chip persis nama dietary preference
                        chipFilterMap[name] = FilterSpec("diet", id)
                    }

                dynamicCategories = chipFilterMap.keys.toList()
                setupCategoryChipsCompose(dynamicCategories)
            } catch (e: Exception) {
                Log.e(logTag, "Failed load diet chips: ${e.message}", e)
                // tetap pakai default chips
            }
        }
    }

    private fun buildDefaultChipFilters() {
        chipFilterMap.clear()
        chipFilterMap["All Menu"] = FilterSpec(null, null)
        chipFilterMap["Popular"] = FilterSpec("popular", null)
        dynamicCategories = chipFilterMap.keys.toList()
    }

    private fun setupCategoryChipsCompose(categories: List<String>) {
        val composeView = findViewById<ComposeView?>(R.id.categoryChipsCompose) ?: return
        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

        // default selected
        if (!categories.contains(selectedCategory)) {
            selectedCategory = categories.firstOrNull() ?: "All Menu"
        }

        composeView.setContent {
            var selected by remember { mutableStateOf(selectedCategory) }
            CategoryChipsRow(
                categories = categories,
                selected = selected,
                onSelected = { newValue ->
                    // set kedua variabel supaya state activity + state compose sama
                    selectedCategory = newValue
                    selected = newValue

                    val spec = chipFilterMap[newValue] ?: FilterSpec(null, null)
                    applyFilterPriority(
                        dietId = if (spec.type == "diet") spec.id else null,
                        ingredientId = if (spec.type == "ingredient") spec.id else null,
                        noAllergyId = if (spec.type == "no_allergy") spec.id else null,
                        popular = spec.type == "popular",
                    )

                    resetAndLoadFirstPage()
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

        gridLayoutManager = GridLayoutManager(this, 2)
        rv.layoutManager = gridLayoutManager
        rv.adapter = adapter

        if (rv.itemDecorationCount == 0) {
            rv.addItemDecoration(
                GridSpacingItemDecoration(spanCount = 2, spacingDp = 14, includeEdge = false)
            )
        }

        // Infinite scroll: load berikutnya saat mendekati bawah
        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) return

                val total = gridLayoutManager.itemCount
                val lastVisible = gridLayoutManager.findLastVisibleItemPosition()

                Log.d(logTag, "onScrolled dy=$dy lastVisible=$lastVisible total=$total isLoading=$isLoading canLoadMore=$canLoadMore")

                if (!isLoading && canLoadMore && lastVisible >= total - 6) {
                    Log.d(logTag, "Trigger loadNextPage: currentPage=$currentPage -> ${currentPage + 1}")
                    loadNextPageIfNeeded()
                }
            }
        })
    }

    private fun setupSearch() {
        val etSearch = findViewById<EditText?>(R.id.etSearch) ?: return

        etSearch.setOnEditorActionListener { v, actionId, event ->
            val isSearchAction = actionId == EditorInfo.IME_ACTION_SEARCH
            val isEnter = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN

            if (isSearchAction || isEnter) {
                currentQuery = v.text?.toString().orEmpty()

                // Search server-side (biar hasilnya sesuai backend paginate)
                resetAndLoadFirstPage()
                true
            } else {
                false
            }
        }
    }

    private fun applyFiltersAndShow() {
        val byCategory = filterByCategory(loadedItems, selectedCategory)

        // sekarang search dilakukan server-side, jadi tidak perlu filter lokal lagi
        showList(byCategory)
    }

    private fun filterByCategory(items: List<RecommendationItem>, category: String): List<RecommendationItem> {
        return when (category) {
            "All Menu" -> items
            else -> items
        }
    }

    private fun showList(items: List<RecommendationItem>) {
        val rv = findViewById<RecyclerView?>(R.id.rvRecipes)
        val empty = findViewById<LinearLayout?>(R.id.emptyRecipesState)
        val spacer = findViewById<View?>(R.id.bottomSpacer)

        adapter.submitList(items.toList())

        val isEmpty = items.isEmpty()
        rv?.visibility = if (isEmpty) View.GONE else View.VISIBLE
        empty?.visibility = if (isEmpty) View.VISIBLE else View.GONE

        spacer?.let {
            val params = it.layoutParams
            params.height = if (isEmpty) resources.getDimensionPixelSize(R.dimen.recipes_bottom_spacer_empty) else resources.getDimensionPixelSize(R.dimen.recipes_bottom_spacer)
            it.layoutParams = params
        }
    }
}
