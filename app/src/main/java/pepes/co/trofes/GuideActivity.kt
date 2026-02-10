package pepes.co.trofes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import pepes.co.trofes.auth.AuthSession
import pepes.co.trofes.auth.BaseAuthActivity
import pepes.co.trofes.data.remote.RetrofitClient
import pepes.co.trofes.data.remote.extractGuides
import pepes.co.trofes.data.remote.lastPage
import pepes.co.trofes.data.remote.nextPageUrl

class GuideActivity : BaseAuthActivity() {

    override fun requiredLoginIntent(): android.content.Intent = SigninIntentFactory.forGuides(this)

    private lateinit var adapter: GuideAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private val loadedItems = mutableListOf<GuideArticle>()

    private var isLoading = false
    private var currentPage = 1
    private var canLoadMore = true
    private val perPage = 10
    private var currentQuery: String = ""

    private val logTag = "GuidesPaging"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isAuthRedirected) return

        setContentView(R.layout.activity_guide)

        setupTopActions()
        syncHeaderAuthState()

        setupBottomNav()
        setupList()
        setupSearch()

        resetAndLoadFirstPage()
    }

    override fun onResume() {
        super.onResume()
        syncHeaderAuthState()
    }

    private fun setupTopActions() {
        findViewById<com.google.android.material.button.MaterialButton?>(R.id.btnLogin)?.setOnClickListener {
            startActivity(SigninIntentFactory.forGuides(this))
        }

        findViewById<android.widget.ImageView?>(R.id.ivProfile)?.setOnClickListener {
            if (authSession.isLoggedIn()) {
                startActivity(Intent(this, ProfileActivity::class.java))
            } else {
                startActivity(SigninIntentFactory.forGuides(this))
            }
        }
    }

    private fun syncHeaderAuthState() {
        val btnLogin = findViewById<com.google.android.material.button.MaterialButton?>(R.id.btnLogin)
        val ivProfile = findViewById<android.widget.ImageView?>(R.id.ivProfile)
        val tvGreeting = findViewById<android.widget.TextView?>(R.id.tvGreeting)

        val loggedIn = authSession.isLoggedIn()
        btnLogin?.visibility = if (loggedIn) android.view.View.GONE else android.view.View.VISIBLE
        ivProfile?.visibility = if (loggedIn) android.view.View.VISIBLE else android.view.View.GONE

        if (loggedIn) {
            val username = authSession.getUser()?.username?.ifBlank { "" }.orEmpty()
            tvGreeting?.text = if (username.isNotBlank()) "Hi $username" else "Hi"
        } else {
            tvGreeting?.text = "Hi"
        }
    }

    private fun setupList() {
        val rv = findViewById<RecyclerView>(R.id.rvGuides)
        layoutManager = LinearLayoutManager(this)
        rv.layoutManager = layoutManager

        adapter = GuideAdapter { guide ->
            if (!authSession.isLoggedIn()) {
                startActivity(
                    Intent(this, SigninActivity::class.java).apply {
                        putExtra(AuthSession.EXTRA_AFTER_LOGIN_TARGET, AuthSession.TARGET_GUIDE_DETAIL)
                        putExtras(GuideDetailActivity.newBundle(guide))
                    }
                )
                return@GuideAdapter
            }

            startActivity(Intent(this, GuideDetailActivity::class.java).apply {
                putExtras(GuideDetailActivity.newBundle(guide))
            })
        }
        rv.adapter = adapter

        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) return

                val total = layoutManager.itemCount
                val lastVisible = layoutManager.findLastVisibleItemPosition()

                Log.d(logTag, "onScrolled dy=$dy lastVisible=$lastVisible total=$total isLoading=$isLoading canLoadMore=$canLoadMore")

                if (!isLoading && canLoadMore && lastVisible >= total - 4) {
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
                resetAndLoadFirstPage()
                true
            } else {
                false
            }
        }
    }

    private fun resetAndLoadFirstPage() {
        loadedItems.clear()
        adapter.submitList(emptyList())

        currentPage = 1
        canLoadMore = true

        fetchGuidesPage(page = currentPage, query = currentQuery)
    }

    private fun loadNextPageIfNeeded() {
        if (isLoading || !canLoadMore) return
        fetchGuidesPage(page = currentPage + 1, query = currentQuery)
    }

    private fun fetchGuidesPage(page: Int, query: String) {
        if (isLoading) return
        if (!canLoadMore) return

        isLoading = true

        Log.d(logTag, "fetchGuidesPage(page=$page perPage=$perPage query='${query}')")

        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.apiService.getGuides(
                    page = page,
                    perPage = perPage,
                    search = query.takeIf { it.isNotBlank() },
                )

                val guides = resp.extractGuides()
                Log.d(logTag, "resp guides.size=${guides.size} nextPageUrl=${resp.nextPageUrl()} lastPage=${resp.lastPage()}")

                val last = resp.lastPage()
                canLoadMore = when {
                    !resp.nextPageUrl().isNullOrBlank() -> true
                    last != null -> page < last
                    else -> guides.isNotEmpty()
                }

                val mapped = guides.map { g ->
                    val id = (g.guideId ?: g.id ?: 0L).toString()
                    val title = g.title.orEmpty()
                    val desc = g.excerpt ?: g.content ?: ""
                    val date = g.publishedAt ?: ""
                    val imageUrl = g.publicImage ?: g.image

                    GuideArticle(
                        id = id,
                        title = title,
                        desc = desc,
                        date = date,
                        imageUrl = imageUrl,
                        content = g.content,
                        slug = g.slug,
                        publishedAt = g.publishedAt,
                    )
                }

                if (page == 1) loadedItems.clear()
                loadedItems.addAll(mapped)

                adapter.submitList(loadedItems.toList())

                currentPage = page

                if (guides.isEmpty()) {
                    canLoadMore = false
                }
            } catch (e: Exception) {
                Log.e(logTag, "fetch error page=$page: ${e.message}", e)
                Toast.makeText(this@GuideActivity, "Gagal memuat guides: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    private fun setupBottomNav() {
        findViewById<BottomNavigationView?>(R.id.bottomNavigation)?.apply {
            selectedItemId = R.id.nav_guide

            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {
                        startActivity(Intent(this@GuideActivity, HomeActivity::class.java))
                        finish()
                        true
                    }

                    R.id.nav_recipes -> {
                        startActivity(Intent(this@GuideActivity, RecipesActivity::class.java))
                        finish()
                        true
                    }

                    R.id.nav_guide -> true

                    R.id.nav_contact -> {
                        startActivity(Intent(this@GuideActivity, CalculatorActivity::class.java))
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
}
