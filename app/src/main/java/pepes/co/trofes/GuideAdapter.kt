package pepes.co.trofes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GuideAdapter(private val items: List<GuideArticle>) : RecyclerView.Adapter<GuideAdapter.GuideViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_guide_article, parent, false)
        return GuideViewHolder(v)
    }
    override fun onBindViewHolder(holder: GuideViewHolder, position: Int) {
        holder.bind(items[position])
    }
    override fun getItemCount() = items.size

    class GuideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ImageView = itemView.findViewById(R.id.ivGuideImage)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvGuideTitle)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvGuideDesc)
        private val tvDate: TextView = itemView.findViewById(R.id.tvGuideDate)
        fun bind(item: GuideArticle) {
            ivImage.setImageResource(item.imageRes)
            tvTitle.text = item.title
            tvDesc.text = item.desc
            tvDate.text = item.date
        }
    }
}
