package pepes.co.trofes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import java.util.Locale

class PopularMenuAdapter(
    private val items: MutableList<PopularMenuItem> = mutableListOf(),
    private val onItemClick: (PopularMenuItem) -> Unit = {}
) : RecyclerView.Adapter<PopularMenuAdapter.VH>() {

    fun submitList(newItems: List<PopularMenuItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_popular_menu_recipe_style, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val maxLikes = items.maxOfOrNull { it.likesCount } ?: 0
        holder.bind(item, isTopLiked = item.likesCount == maxLikes && maxLikes > 0)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivThumb: ImageView = itemView.findViewById(R.id.ivPopImage)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvPopTitle)
        private val tvRating: TextView = itemView.findViewById(R.id.tvPopRating)
        private val pbRating: ProgressBar = itemView.findViewById(R.id.pbPopRating)
        private val tvMeta: TextView = itemView.findViewById(R.id.tvPopMeta)
        private val chipTag: Chip = itemView.findViewById(R.id.chipPopTag)
        private val badgePopular: View = itemView.findViewById(R.id.badgePopular)

        fun bind(item: PopularMenuItem, isTopLiked: Boolean) {
            ivThumb.setImageResource(item.imageRes)
            tvTitle.text = item.title

            val ratingValue = item.rating.toFloatOrNull()?.coerceIn(0f, 5f) ?: 0f
            tvRating.text = String.format(Locale.US, "%.1f", ratingValue)
            pbRating.progress = ((ratingValue / 5f) * 10000f).toInt().coerceIn(0, 10000)

            tvMeta.text = item.meta
            chipTag.text = item.tag
            badgePopular.visibility = if (isTopLiked) View.VISIBLE else View.GONE
        }
    }
}
