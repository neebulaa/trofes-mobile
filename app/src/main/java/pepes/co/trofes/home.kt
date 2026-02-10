package pepes.co.trofes

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import pepes.co.trofes.auth.AuthSession
import pepes.co.trofes.auth.requireAuth
import pepes.co.trofes.home.HomeViewModel
import pepes.co.trofes.ui.CategoryChipsRow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.viewpager2.widget.ViewPager2
import androidx.lifecycle.Lifecycle
import pepes.co.trofes.SigninIntentFactory

class HomeActivity : AppCompatActivity() {

    // List bawah (Popular menus) sekarang menggunakan card yang sama seperti rekomendasi,
    // tapi orientasinya vertikal ke bawah.
    private lateinit var popularAdapter: RecommendationAdapter
    private var allPopularItems: List<PopularMenuItem> = emptyList()

    private lateinit var recommendationAdapter: RecommendationAdapter

    // simpan semua rekomendasi untuk filter
    private var allRecommendations: List<RecommendationItem> = emptyList()

    // cache mapping popular -> recommendation model, biar filter/search gampang
    private var popularAsRecommendations: List<RecommendationItem> = emptyList()

    private var selectedCategory: String = "All Menu"

    private val heroHandler = Handler(Looper.getMainLooper())
    private var heroRunnable: Runnable? = null

    private lateinit var homeVm: HomeViewModel

    private lateinit var authSession: AuthSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Wajib login sebelum pakai
        if (requireAuth()) return

        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        authSession = AuthSession(this)


        // toggle header login/profile
        syncHeaderAuthState()

        homeVm = ViewModelProvider(this)[HomeViewModel::class.java]

        setupTopActions()
        setupPopularList()
        setupBottomNav()
        setupSearch()
        setupRecommendationCompose()
        setupCategoryChipsCompose()

        observeHomeApi()
        homeVm.loadHome()
    }

    override fun onResume() {
        super.onResume()
        syncHeaderAuthState()
    }

    private fun syncHeaderAuthState() {
        val btnLogin = findViewById<com.google.android.material.button.MaterialButton?>(R.id.btnLogin)
        val ivProfile = findViewById<ImageView?>(R.id.ivProfile)
        val tvGreeting = findViewById<TextView?>(R.id.tvGreeting)

        val loggedIn = authSession.isLoggedIn()
        btnLogin?.visibility = if (loggedIn) View.GONE else View.VISIBLE
        ivProfile?.visibility = if (loggedIn) View.VISIBLE else View.GONE

        // kiri: "Hi {username}"
        if (loggedIn) {
            val username = authSession.getUser()?.username?.ifBlank { "" }.orEmpty()
            tvGreeting?.text = if (username.isNotBlank()) "Hi $username" else "Hi"
        } else {
            tvGreeting?.text = "Hi"
        }

        btnLogin?.setOnClickListener {
            startActivity(SigninIntentFactory.forHome(this))
        }
    }

    private fun observeHomeApi() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeVm.state.collect { st ->
                    if (st.errorMessage != null) {
                        Toast.makeText(this@HomeActivity, st.errorMessage, Toast.LENGTH_SHORT).show()
                    }

                    // Hero dari API
                    if (st.hero.isNotEmpty()) {
                        setupHeroSlider(st.hero)
                    }

                    // Our Recommendation dari API
                    if (st.recommendations.isNotEmpty()) {
                        allRecommendations = st.recommendations
                        // penting: clear dulu agar fallback "Loading..." tidak tersisa
                        recommendationAdapter.submitList(emptyList())
                        recommendationAdapter.submitList(allRecommendations)
                    }

                    // Popular (list bawah) dari API
                    if (st.popular.isNotEmpty()) {
                        popularAsRecommendations = st.popular
                        // penting: clear dulu agar fallback "Loading..." tidak tersisa
                        popularAdapter.submitList(emptyList())
                        applyCategoryFilter(selectedCategory)
                    }
                }
            }
        }
    }

    private fun setupTopActions() {
        findViewById<ImageView?>(R.id.ivProfile)?.setOnClickListener {
            if (authSession.isLoggedIn()) {
                startActivity(Intent(this, ProfileActivity::class.java))
            } else {
                startActivity(
                    Intent(this, SigninActivity::class.java).apply {
                        putExtra(AuthSession.EXTRA_AFTER_LOGIN_TARGET, AuthSession.TARGET_HOME)
                    }
                )
            }
        }

        findViewById<ImageButton?>(R.id.btnFilter)?.setOnClickListener {
            Toast.makeText(this, "Filter clicked", Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView?>(R.id.tvViewAll)?.setOnClickListener {
            Toast.makeText(this, "View all recommendation", Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView?>(R.id.tvPopularViewAll)?.setOnClickListener {
            Toast.makeText(this, "View all popular menus", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setPopularEmptyState(@Suppress("UNUSED_PARAMETER") isEmpty: Boolean) {
        // Empty state view sudah dihapus dari layout Home.
        // Kita biarkan fungsi ini no-op supaya pemanggil lama tidak menyebabkan crash.
        // Kalau nanti kamu mau empty state lagi, kita bisa re-add dan hidupkan ulang.
    }

    private fun setupPopularList() {
        // Setelah perubahan XML: list bawah sekarang pakai id rvRecipes (bukan rvPopular)
        val rv = findViewById<RecyclerView>(R.id.rvRecipes)

        popularAdapter = RecommendationAdapter(
            onItemClick = { item ->
                if (!authSession.isLoggedIn()) {
                    startActivity(
                        SigninIntentFactory.forRecipeDetail(this, item)
                    )
                    return@RecommendationAdapter
                }
                startActivity(RecipeDetailComposeActivity.newIntent(this, item))
            },
            itemLayoutRes = R.layout.item_recommendation_grid,
        )

        // Popular = GRID 2 kolom (scroll ke bawah mengikuti NestedScrollView)
        rv.layoutManager = GridLayoutManager(this, 2)
        rv.adapter = popularAdapter

        // Spacing grid supaya rapih (sama seperti Recipes)
        if (rv.itemDecorationCount == 0) {
            rv.addItemDecoration(GridSpacingItemDecoration(spanCount = 2, spacingDp = 14, includeEdge = false))
        }

        // fallback hanya untuk state awal sebelum API datang
        if (popularAsRecommendations.isEmpty()) {
            popularAsRecommendations = listOf(
                RecommendationItem(
                    id = "fallback_popular",
                    title = "Loading...",
                    rating = "0.0",
                    likesCount = 0,
                    caloriesText = "0",
                    timeText = "0m",
                    tagText = "-",
                    category = "All Menu",
                    imageRes = R.drawable.berita_1_,
                    firstDietaryPreference = "-",
                )
            )
            applyCategoryFilter(selectedCategory)
        }

        // apply filter awal (default All Menu)
        applyCategoryFilter(selectedCategory)
    }

    private fun setupBottomNav() {
        // Bottom menu
        findViewById<BottomNavigationView?>(R.id.bottomNavigation)?.apply {
            // pastikan item Home ter-select saat berada di Home
            selectedItemId = R.id.nav_home

            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> true

                    R.id.nav_recipes -> {
                        startActivity(Intent(this@HomeActivity, RecipesActivity::class.java))
                        true
                    }

                    R.id.nav_guide -> {
                        startActivity(Intent(this@HomeActivity, GuideActivity::class.java))
                        true
                    }

                    R.id.nav_contact -> {
                        startActivity(Intent(this@HomeActivity, CalculatorActivity::class.java))
                        true
                    }

                    // placeholder (slot FAB)
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

    private fun setupSearch() {
        val etSearch = findViewById<EditText?>(R.id.etSearch) ?: return

        etSearch.setOnEditorActionListener { v, actionId, event ->
            val isSearchAction = actionId == EditorInfo.IME_ACTION_SEARCH
            val isEnter = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN

            if (isSearchAction || isEnter) {
                applySearch(v.text?.toString().orEmpty())
                true
            } else {
                false
            }
        }
    }

    private fun applySearch(queryRaw: String) {
        val q = queryRaw.trim()
        if (q.isEmpty()) {
            // apply filter awal (default All Menu)
            applyCategoryFilter(selectedCategory)
            return
        }

        // search harus menghormati filter kategori yang sedang dipilih
        val base = filteredByCategory(selectedCategory)
        val filtered = base.filter {
            it.title.contains(q, ignoreCase = true) ||
                it.tagText.contains(q, ignoreCase = true) ||
                it.timeText.contains(q, ignoreCase = true) ||
                it.caloriesText.contains(q, ignoreCase = true)
        }

        popularAdapter.submitList(filtered)
    }

    private fun setupCategoryChipsCompose() {
        val composeView = findViewById<ComposeView?>(R.id.categoryChipsCompose) ?: return

        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

        // Utama
        val main = listOf("All Menu", "Popular")
        // Sisanya (bisa banyak)
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

        // Random urutan untuk yang extra (biar kelihatan variatif)
        val categories = main + extra.shuffled()

        composeView.setContent {
            var selected by remember { mutableStateOf(selectedCategory) }

            CategoryChipsRow(
                categories = categories,
                selected = selected,
                onSelected = { newValue ->
                    // kita simpan ke Activity state supaya survive recomposition
                    selectedCategory = newValue
                    selected = newValue
                    applyCategoryFilter(newValue)
                },
            )
        }
    }

    private fun applyCategoryFilter(category: String) {
        val filtered = filteredByCategory(category)
        popularAdapter.submitList(filtered)
    }

    private fun filteredByCategory(category: String): List<RecommendationItem> {
        return if (category == "Popular") {
            // Popular = top 10 by likes (desc) hanya untuk list bawah
            popularAsRecommendations
                .sortedByDescending { it.likesCount + if (it.isLiked) 1 else 0 }
                .take(10)
        } else {
            fun matchCategory(itemCategory: String): Boolean {
                if (category == "All Menu") return true
                return itemCategory.equals(category, ignoreCase = true)
            }
            popularAsRecommendations.filter { matchCategory(it.category) }
        }
    }

    private fun parseMetaToTimeCalories(meta: String): Pair<String, String> {
        // meta contoh: "10 min · 200 kcal"
        val parts = meta.split("·").map { it.trim() }
        val timeText = parts.getOrNull(0)?.replace(" ", "")?.replace("mins", "m")?.replace("min", "m") ?: "10m"
        val calories = parts.getOrNull(1)
            ?.replace("kcal", "", ignoreCase = true)
            ?.trim()
            ?: "200"
        return timeText to calories
    }

    private fun setupRecommendationCompose() {
        // Our Recommendation = HORIZONTAL (scroll ke samping)
        val rv = findViewById<RecyclerView?>(R.id.recommendationCompose) ?: return

        recommendationAdapter = RecommendationAdapter(
            onItemClick = { item ->
                if (!authSession.isLoggedIn()) {
                    startActivity(
                        SigninIntentFactory.forRecipeDetail(this, item)
                    )
                    return@RecommendationAdapter
                }
                startActivity(RecipeDetailComposeActivity.newIntent(this@HomeActivity, item))
            },
            itemLayoutRes = R.layout.item_recommendation,
        )

        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv.adapter = recommendationAdapter

        // horizontal jangan pakai grid spacing
        // (kalau sebelumnya sudah ada decoration dari versi grid, kita bersihkan biar ga mengganggu)
        while (rv.itemDecorationCount > 0) {
            rv.removeItemDecorationAt(0)
        }

        rv.overScrollMode = View.OVER_SCROLL_NEVER

        // fallback hanya untuk state awal sebelum API datang
        if (allRecommendations.isEmpty()) {
            recommendationAdapter.submitList(
                listOf(
                    RecommendationItem(
                        id = "fallback",
                        title = "Loading...",
                        rating = "0.0",
                        likesCount = 0,
                        caloriesText = "0",
                        timeText = "0m",
                        tagText = "-",
                        category = "All Menu",
                        imageRes = R.drawable.banner__1_,
                        firstDietaryPreference = "-",
                    )
                )
            )
        }
    }

    private fun setupHeroSlider(items: List<HeroBannerItem>) {
        val vp = findViewById<ViewPager2?>(R.id.vpHero) ?: return
        val dots = findViewById<android.widget.LinearLayout?>(R.id.heroDots) ?: return

        // stop auto slide sebelumnya
        heroRunnable?.let { heroHandler.removeCallbacks(it) }

        vp.adapter = HeroBannerAdapter(items)
        vp.offscreenPageLimit = 1
        vp.getChildAt(0)?.overScrollMode = View.OVER_SCROLL_NEVER

        // cache dots view biar ga recreate object berat terus
        val dotViews = ArrayList<View>(items.size)

        fun buildDots() {
            dots.removeAllViews()
            dotViews.clear()
            repeat(items.size) {
                val dot = View(this)
                val lp = android.widget.LinearLayout.LayoutParams(6.dp(), 6.dp())
                lp.marginStart = 6.dp()
                lp.marginEnd = 6.dp()
                dot.layoutParams = lp
                dot.background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                    cornerRadius = 50f
                    setColor(0x80FFFFFF.toInt())
                }
                dots.addView(dot)
                dotViews.add(dot)
            }
        }

        fun selectDot(index: Int) {
            if (dotViews.isEmpty()) return
            dotViews.forEachIndexed { i, v ->
                val w = if (i == index) 14.dp() else 6.dp()
                val params = v.layoutParams as android.widget.LinearLayout.LayoutParams
                params.width = w
                params.height = 6.dp()
                v.layoutParams = params
                (v.background as? android.graphics.drawable.GradientDrawable)?.setColor(
                    if (i == index) android.graphics.Color.WHITE else 0x80FFFFFF.toInt()
                )
            }
        }

        buildDots()
        selectDot(0)

        vp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                selectDot(position)
            }
        })

        // auto slide tiap 3 detik (kalau item > 1)
        heroRunnable = if (items.size > 1) {
            object : Runnable {
                override fun run() {
                    val next = (vp.currentItem + 1) % items.size
                    vp.setCurrentItem(next, true)
                    heroHandler.postDelayed(this, 3000)
                }
            }
        } else null

        heroRunnable?.let { heroHandler.postDelayed(it, 3000) }
    }

    private fun setupHeroSlider() {
        // fallback dummy jika API belum kebaca
        setupHeroSlider(
            listOf(
                HeroBannerItem(imageRes = R.drawable.banner__1_, title = "Stay healty"),
                HeroBannerItem(imageRes = R.drawable.banner__1_, title = "Eat smart"),
                HeroBannerItem(imageRes = R.drawable.banner__1_, title = "Find your menu"),
            )
        )
    }

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()
}