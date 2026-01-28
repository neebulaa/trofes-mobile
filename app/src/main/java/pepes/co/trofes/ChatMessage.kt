package pepes.co.trofes

data class ChatMessage(
    val id: String,
    val text: String,
    val timeText: String,
    val fromUser: Boolean,
)
