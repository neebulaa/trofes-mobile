package pepes.co.trofes

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HeroBannerAdapter(
    private val items: List<HeroBannerItem>,
) : RecyclerView.Adapter<HeroBannerAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hero_banner, parent, false)
        return VH(view as android.widget.FrameLayout)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class VH(itemView: android.widget.FrameLayout) : RecyclerView.ViewHolder(itemView) {
        private val iv: ImageView = itemView.findViewById(R.id.ivBanner)
        private val tv: TextView = itemView.findViewById(R.id.tvBannerTitle)

        fun bind(item: HeroBannerItem) {
            iv.setImageResource(item.imageRes)
            tv.text = item.title
        }
    }
}
