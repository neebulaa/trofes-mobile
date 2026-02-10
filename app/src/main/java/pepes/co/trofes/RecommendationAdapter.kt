package pepes.co.trofes

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import java.util.Locale

class RecommendationAdapter(
    private val onItemClick: (RecommendationItem) -> Unit,
    private val itemLayoutRes: Int = R.layout.item_recommendation,
) : ListAdapter<RecommendationItem, RecommendationAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(itemLayoutRes, parent, false)
        return VH(view as com.google.android.material.card.MaterialCardView, onItemClick) { updated ->
            // Update item state inside ListAdapter (submitList copy)
            val newList = currentList.map { if (it.id == updated.id) updated else it }
            submitList(newList)
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val maxLikes = currentList.maxOfOrNull { it.likesCount + if (it.isLiked) 1 else 0 } ?: 0
        val shownLikes = item.likesCount + if (item.isLiked) 1 else 0
        holder.bind(item, isTopLiked = shownLikes == maxLikes && maxLikes > 0)
    }

    class VH(
        itemView: com.google.android.material.card.MaterialCardView,
        private val onItemClick: (RecommendationItem) -> Unit,
        private val onToggleLike: (RecommendationItem) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {

        private val ivImage: ImageView = itemView.findViewById(R.id.ivRecImage)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvRecTitle)
        private val tvLikes: TextView = itemView.findViewById(R.id.tvRecLikes)
        private val tvCalories: TextView = itemView.findViewById(R.id.tvRecCalories)
        private val tvTime: TextView = itemView.findViewById(R.id.tvRecTime)
        private val tvTag: TextView = itemView.findViewById(R.id.tvRecTag)
        private val tagContainer: android.view.View? = runCatching { itemView.findViewById<android.view.View>(R.id.tagContainer) }.getOrNull()
        private val badgePopular: android.view.View = itemView.findViewById(R.id.badgePopular)
        private val btnLike: ImageButton = itemView.findViewById(R.id.btnRecLike)

        // rating compact
        private val pbRating: ProgressBar = itemView.findViewById(R.id.pbRecRating)
        private val tvRating: TextView = itemView.findViewById(R.id.tvRecRating)

        fun bind(item: RecommendationItem, isTopLiked: Boolean) {
            val placeholderRes = item.imageRes ?: R.drawable.berita_1_
            ivImage.load(item.imageUrl) {
                placeholder(placeholderRes)
                error(placeholderRes)
                crossfade(true)
            }

            tvTitle.text = item.title

            // likes yang ditampilkan mengikuti state like
            val shownLikes = item.likesCount + if (item.isLiked) 1 else 0
            tvLikes.text = shownLikes.toString()

            // ikon daun = total bahan (total_ingredient)
            tvCalories.text = item.ingredientsCount.toString()
            tvTime.text = item.timeText

            // Chip hijau: pakai dietary preference pertama (tagText). Kalau kosong -> hide.
            val tag = item.tagText.trim()
            tvTag.text = tag
            val showTag = tag.isNotBlank()
            tvTag.visibility = if (showTag) android.view.View.VISIBLE else android.view.View.GONE
            tagContainer?.visibility = if (showTag) android.view.View.VISIBLE else android.view.View.GONE

            // Rating: 1 star yang terisi sesuai rating/5 + angka
            val ratingValue = item.rating.toFloatOrNull()?.coerceIn(0f, 5f) ?: 0f
            tvRating.text = String.format(Locale.US, "%.1f", ratingValue)
            pbRating.progress = ((ratingValue / 5f) * 10000f).toInt().coerceIn(0, 10000)

            // selector drawable + selector tint akan mengikuti state selected
            btnLike.isSelected = item.isLiked

            badgePopular.visibility = if (isTopLiked) android.view.View.VISIBLE else android.view.View.GONE

            btnLike.setOnClickListener {
                // toggle state + update ui model
                val updated = item.copy(isLiked = !item.isLiked)
                onToggleLike(updated)
            }

            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<RecommendationItem>() {
            override fun areItemsTheSame(oldItem: RecommendationItem, newItem: RecommendationItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: RecommendationItem, newItem: RecommendationItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
