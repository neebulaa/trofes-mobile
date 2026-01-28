package pepes.co.trofes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CountryAdapter(
    private var items: List<Country>,
    private val onClick: (Country) -> Unit,
) : RecyclerView.Adapter<CountryAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFlag: TextView = itemView.findViewById(R.id.tvFlag)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvDialCode: TextView = itemView.findViewById(R.id.tvDialCode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_country, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = items[position]
        holder.tvFlag.text = c.flagEmoji
        holder.tvName.text = c.name
        holder.tvDialCode.text = c.dialCode
        holder.itemView.setOnClickListener { onClick(c) }
    }

    fun submitList(newItems: List<Country>) {
        items = newItems
        notifyDataSetChanged()
    }
}

