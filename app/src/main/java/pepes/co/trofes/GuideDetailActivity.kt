package pepes.co.trofes

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

/**
 * Halaman detail guide sederhana (dummy) untuk memenuhi requirement gating.
 * Nanti bisa diganti menjadi fetch dari endpoint /guides/{id}.
 */
class GuideDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_guide_detail)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<TextView>(R.id.tvTitle).text = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        findViewById<TextView>(R.id.tvDate).text = intent.getStringExtra(EXTRA_DATE).orEmpty()
        findViewById<TextView>(R.id.tvContent).text = intent.getStringExtra(EXTRA_DESC).orEmpty()

        intent.getIntExtra(EXTRA_IMAGE, 0).takeIf { it != 0 }?.let {
            findViewById<ImageView>(R.id.ivImage).setImageResource(it)
        }
    }

    companion object {
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_DESC = "extra_desc"
        private const val EXTRA_DATE = "extra_date"
        private const val EXTRA_IMAGE = "extra_image"

        fun newBundle(item: GuideArticle) = Bundle().apply {
            putString(EXTRA_TITLE, item.title)
            putString(EXTRA_DESC, item.desc)
            putString(EXTRA_DATE, item.date)
            putInt(EXTRA_IMAGE, item.imageRes)
        }
    }
}
