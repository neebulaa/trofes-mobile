package pepes.co.trofes

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ChatMessageAdapter : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DIFF) {

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).fromUser) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 1) {
            val v = inflater.inflate(R.layout.item_chat_message_user, parent, false)
            UserVH(v as androidx.constraintlayout.widget.ConstraintLayout)
        } else {
            val v = inflater.inflate(R.layout.item_chat_message_bot, parent, false)
            BotVH(v as androidx.constraintlayout.widget.ConstraintLayout)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is BotVH -> holder.bind(item)
            is UserVH -> holder.bind(item)
        }
    }

    class BotVH(private val root: androidx.constraintlayout.widget.ConstraintLayout) : RecyclerView.ViewHolder(root) {
        private val tvMsg: TextView = root.findViewById(R.id.tvMessage)
        private val tvTime: TextView = root.findViewById(R.id.tvTime)
        fun bind(item: ChatMessage) {
            tvMsg.text = item.text
            tvTime.text = item.timeText
        }
    }

    class UserVH(private val root: androidx.constraintlayout.widget.ConstraintLayout) : RecyclerView.ViewHolder(root) {
        private val tvMsg: TextView = root.findViewById(R.id.tvMessage)
        private val tvTime: TextView = root.findViewById(R.id.tvTime)
        fun bind(item: ChatMessage) {
            tvMsg.text = item.text
            tvTime.text = item.timeText
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean = oldItem == newItem
        }
    }
}
