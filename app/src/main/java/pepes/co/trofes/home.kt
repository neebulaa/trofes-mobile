package pepes.co.trofes

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import pepes.co.trofes.ui.CategoryChipsRow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.viewpager2.widget.ViewPager2

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        setupTopActions()
        setupHeroSlider()
        setupPopularList()
        setupBottomNav()
        setupSearch()
        setupRecommendationCompose()
        setupCategoryChipsCompose()
    }

    override fun onStart() {
        super.onStart()
        // restart auto-slide ketika balik ke Home
        heroRunnable?.let { heroHandler.postDelayed(it, 3000) }
    }

    override fun onStop() {
        super.onStop()
        heroRunnable?.let { heroHandler.removeCallbacks(it) }
    }

    private fun setupTopActions() {
        findViewById<ImageView?>(R.id.ivProfile)?.setOnClickListener {
            startActivity(Intent(this, SigninActivity::class.java))
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
            onItemClick = { Toast.makeText(this, it.title, Toast.LENGTH_SHORT).show() },
            // samakan dengan halaman Recipes (grid card)
            itemLayoutRes = R.layout.item_recommendation_grid,
        )

        // Popular = GRID 2 kolom (scroll ke bawah mengikuti NestedScrollView)
        rv.layoutManager = GridLayoutManager(this, 2)
        rv.adapter = popularAdapter

        // Spacing grid supaya rapih (sama seperti Recipes)
        if (rv.itemDecorationCount == 0) {
            rv.addItemDecoration(GridSpacingItemDecoration(spanCount = 2, spacingDp = 14, includeEdge = false))
        }

        // pilih kategori ekstra secara acak supaya contoh terasa banyak
        val extraCategories = listOf(
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

        // Buat data dummy lebih banyak supaya bisa scroll panjang
        val images = listOf(
            R.drawable.sample_food_1,
            R.drawable.sample_food_2,
            R.drawable.sample_food_hero,
            R.drawable.berita_1_,
            R.drawable.banner__1_,
        )

        // rating demo 1..5 + variasi likes agar sorting Popular masuk akal
        val ratings = listOf("1.0", "2.0", "2.5", "3.0", "4.0", "5.0")

        allPopularItems = (1..80).map { i ->
            val rating = ratings.random()
            val likes = when (rating) {
                "5.0" -> 2000 + i * 3
                "4.0" -> 800 + i * 2
                "3.0" -> 300 + i
                "2.5" -> 200 + i
                "2.0" -> 120 + i
                else -> 40 + i
            }
            PopularMenuItem(
                title = "Popular Menu $i",
                rating = rating,
                meta = "${10 + (i % 30)} min · ${180 + (i % 200)} kcal",
                tag = if (i % 9 == 0) "Halal" else "Demo",
                category = extraCategories.random(),
                likesCount = likes,
                imageRes = images[i % images.size],
            )
        }

        // mapping ke model card rekomendasi supaya UI sama persis
        popularAsRecommendations = allPopularItems.mapIndexed { idx, it ->
            val (timeText, caloriesText) = parseMetaToTimeCalories(it.meta)
            RecommendationItem(
                id = "p$idx",
                title = it.title,
                rating = it.rating,
                likesCount = it.likesCount,
                caloriesText = caloriesText,
                timeText = timeText,
                tagText = it.tag,
                category = it.category,
                imageRes = it.imageRes,
                isLiked = false,
            )
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
                        startActivity(Intent(this@HomeActivity, ContactUsActivity::class.java))
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
            onItemClick = { Toast.makeText(this, it.title, Toast.LENGTH_SHORT).show() },
            // pakai layout compact (yang sudah ada) untuk strip horizontal
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

        val extraCategories = listOf(
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

        // Contoh rating 1..5 untuk demo progress bintang pada strip rekomendasi
        allRecommendations = listOf(
            RecommendationItem("r1", "Rating 1.0", "1.0", 34, "320", "12m", "Demo", extraCategories.random(), R.drawable.banner__1_),
            RecommendationItem("r2", "Rating 2.0", "2.0", 56, "340", "14m", "Demo", extraCategories.random(), R.drawable.berita_1_),
            RecommendationItem("r25", "Rating 2.5", "2.5", 78, "360", "16m", "Demo", extraCategories.random(), R.drawable.banner__1_),
            RecommendationItem("r3", "Rating 3.0", "3.0", 120, "380", "18m", "Demo", extraCategories.random(), R.drawable.berita_1_),
            RecommendationItem("r4", "Rating 4.0", "4.0", 500, "500", "36m", "Halal", "Halal", R.drawable.berita_1_),
            RecommendationItem("r5", "Rating 5.0", "5.0", 999, "520", "22m", "Popular", extraCategories.random(), R.drawable.banner__1_),
        )

        recommendationAdapter.submitList(allRecommendations)
    }

    private fun setupHeroSlider() {
        val vp = findViewById<ViewPager2?>(R.id.vpHero) ?: return
        val dots = findViewById<android.widget.LinearLayout?>(R.id.heroDots) ?: return

        val items = listOf(
            HeroBannerItem(R.drawable.banner__1_, "Stay healty"),
            HeroBannerItem(R.drawable.banner__1_, "Eat smart"),
            HeroBannerItem(R.drawable.banner__1_, "Find your menu"),
        )

        vp.adapter = HeroBannerAdapter(items)
        vp.offscreenPageLimit = 1
        vp.getChildAt(0)?.overScrollMode = View.OVER_SCROLL_NEVER

        // cache dots view biar ga recreate object berat terus
        val dotViews = ArrayList<View>(items.size)

        fun buildDotsIfNeeded() {
            if (dotViews.isNotEmpty()) return
            dots.removeAllViews()
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
            buildDotsIfNeeded()
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

        selectDot(0)

        vp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                selectDot(position)
            }
        })

        // auto slide tiap 3 detik
        heroRunnable = object : Runnable {
            override fun run() {
                val next = (vp.currentItem + 1) % items.size
                vp.setCurrentItem(next, true)
                heroHandler.postDelayed(this, 3000)
            }
        }

        // start
        heroHandler.removeCallbacksAndMessages(null)
        heroHandler.postDelayed(heroRunnable!!, 3000)
    }

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()
}