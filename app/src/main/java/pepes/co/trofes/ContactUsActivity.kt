package pepes.co.trofes

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ContactUsActivity : AppCompatActivity() {

    private lateinit var adapter: ContactThreadAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_contact_us)

        setupList()
        setupBottomNav()
    }

    private fun setupList() {
        val rv = findViewById<RecyclerView>(R.id.rvContacts)
        adapter = ContactThreadAdapter(onClick = { thread ->
            val intent = Intent(this@ContactUsActivity, ChatActivity::class.java)
            intent.putExtra(ChatActivity.EXTRA_THREAD_NAME, thread.name)
            startActivity(intent)
        })

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        // dummy threads seperti screenshot
        adapter.submitList(
            listOf(
                ContactThread(
                    id = "admin",
                    name = "Admin Trofes",
                    lastMessage = "Sounds awesome!",
                    timeText = "19:37",
                    unreadCount = 1,
                    isOnline = true,
                ),
                ContactThread(
                    id = "bot",
                    name = "Trofes Bot",
                    lastMessage = "Tanya apa saja",
                    timeText = "19:37",
                    unreadCount = 2,
                    isOnline = true,
                ),
            )
        )
    }

    private fun setupBottomNav() {
        findViewById<BottomNavigationView?>(R.id.bottomNavigation)?.apply {
            selectedItemId = R.id.nav_contact

            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {
                        startActivity(Intent(this@ContactUsActivity, HomeActivity::class.java))
                        finish()
                        true
                    }

                    R.id.nav_recipes -> {
                        startActivity(Intent(this@ContactUsActivity, RecipesActivity::class.java))
                        finish()
                        true
                    }

                    R.id.nav_guide -> {
                        startActivity(Intent(this@ContactUsActivity, GuideActivity::class.java))
                        finish()
                        true
                    }

                    R.id.nav_contact -> true

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
