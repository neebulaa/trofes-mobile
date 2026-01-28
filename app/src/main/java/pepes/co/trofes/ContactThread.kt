package pepes.co.trofes

data class ContactThread(
    val id: String,
    val name: String,
    val lastMessage: String,
    val timeText: String,
    val unreadCount: Int,
    val isOnline: Boolean = true,
)
