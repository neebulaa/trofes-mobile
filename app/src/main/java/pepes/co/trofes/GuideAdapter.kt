package pepes.co.trofes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load

class GuideAdapter(
    private val onItemClick: (GuideArticle) -> Unit,
) : ListAdapter<GuideArticle, GuideAdapter.GuideViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_guide_article, parent, false)
        return GuideViewHolder(v, onItemClick)
    }

    override fun onBindViewHolder(holder: GuideViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class GuideViewHolder(
        itemView: View,
        private val onItemClick: (GuideArticle) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ImageView = itemView.findViewById(R.id.ivGuideImage)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvGuideTitle)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvGuideDesc)
        private val tvDate: TextView = itemView.findViewById(R.id.tvGuideDate)

        fun bind(item: GuideArticle) {
            val placeholderRes = R.drawable.guide_img_1
            if (!item.imageUrl.isNullOrBlank()) {
                ivImage.load(item.imageUrl) {
                    placeholder(placeholderRes)
                    error(placeholderRes)
                    crossfade(true)
                }
            } else {
                // fallback: pakai imageRes kalau ada
                val res = item.imageRes ?: placeholderRes
                ivImage.setImageResource(res)
            }

            tvTitle.text = item.title
            tvDesc.text = item.desc
            tvDate.text = item.date

            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<GuideArticle>() {
            override fun areItemsTheSame(oldItem: GuideArticle, newItem: GuideArticle): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: GuideArticle, newItem: GuideArticle): Boolean {
                return oldItem == newItem
            }
        }
    }
}
