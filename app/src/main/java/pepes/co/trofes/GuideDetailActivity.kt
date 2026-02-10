package pepes.co.trofes

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import coil.load
import pepes.co.trofes.auth.BaseAuthActivity

/**
 * Halaman detail guide sederhana untuk memenuhi requirement gating.
 * Sekarang bisa menampilkan konten dari API (content + imageUrl).
 */
class GuideDetailActivity : BaseAuthActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isAuthRedirected) return

        enableEdgeToEdge()
        setContentView(R.layout.activity_guide_detail)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val date = intent.getStringExtra(EXTRA_DATE).orEmpty()
        val content = intent.getStringExtra(EXTRA_CONTENT)
        val desc = intent.getStringExtra(EXTRA_DESC).orEmpty()
        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)

        findViewById<TextView>(R.id.tvTitle).text = title
        findViewById<TextView>(R.id.tvDate).text = date
        findViewById<TextView>(R.id.tvContent).text = content?.ifBlank { desc } ?: desc

        val iv = findViewById<ImageView>(R.id.ivImage)
        if (!imageUrl.isNullOrBlank()) {
            iv.load(imageUrl) {
                placeholder(R.drawable.guide_img_1)
                error(R.drawable.guide_img_1)
                crossfade(true)
            }
        } else {
            intent.getIntExtra(EXTRA_IMAGE_RES, 0).takeIf { it != 0 }?.let {
                iv.setImageResource(it)
            }
        }
    }

    override fun requiredLoginIntent(): android.content.Intent {
        return android.content.Intent(this, SigninActivity::class.java).apply {
            putExtra(pepes.co.trofes.auth.AuthSession.EXTRA_AFTER_LOGIN_TARGET, pepes.co.trofes.auth.AuthSession.TARGET_GUIDE_DETAIL)
            putExtras(intent.extras ?: Bundle())
        }
    }

    companion object {
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_DESC = "extra_desc"
        private const val EXTRA_DATE = "extra_date"
        private const val EXTRA_IMAGE_RES = "extra_image"
        private const val EXTRA_IMAGE_URL = "extra_image_url"
        private const val EXTRA_CONTENT = "extra_content"

        fun newBundle(item: GuideArticle) = Bundle().apply {
            putString(EXTRA_TITLE, item.title)
            putString(EXTRA_DESC, item.desc)
            putString(EXTRA_CONTENT, item.content)
            putString(EXTRA_DATE, item.date)
            putInt(EXTRA_IMAGE_RES, item.imageRes ?: 0)
            putString(EXTRA_IMAGE_URL, item.imageUrl)
        }
    }
}
