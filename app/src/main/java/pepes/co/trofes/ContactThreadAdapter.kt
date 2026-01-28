package pepes.co.trofes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class ContactThreadAdapter(
    private val items: MutableList<ContactThread> = mutableListOf(),
    private val onClick: (ContactThread) -> Unit = {},
) : RecyclerView.Adapter<ContactThreadAdapter.VH>() {

    fun submitList(newItems: List<ContactThread>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact_thread, parent, false)
        return VH(v as MaterialCardView, onClick)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    class VH(
        private val card: MaterialCardView,
        private val onClick: (ContactThread) -> Unit,
    ) : RecyclerView.ViewHolder(card) {

        private val tvName: TextView = card.findViewById(R.id.tvName)
        private val tvLast: TextView = card.findViewById(R.id.tvLastMessage)
        private val tvTime: TextView = card.findViewById(R.id.tvTime)
        private val badge: View = card.findViewById(R.id.badgeUnread)
        private val tvUnread: TextView = card.findViewById(R.id.tvUnread)
        private val onlineDot: View = card.findViewById(R.id.vOnlineDot)

        fun bind(item: ContactThread) {
            tvName.text = item.name
            tvLast.text = item.lastMessage
            tvTime.text = item.timeText

            onlineDot.visibility = if (item.isOnline) View.VISIBLE else View.GONE

            val unread = item.unreadCount
            badge.visibility = if (unread > 0) View.VISIBLE else View.GONE
            tvUnread.text = unread.coerceAtMost(99).toString()

            card.setOnClickListener { onClick(item) }
        }
    }
}
