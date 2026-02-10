package pepes.co.trofes.data.remote

import com.google.gson.annotations.SerializedName

// Messages list

data class MessagesApiResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: MessagesApiData? = null,
)

data class MessagesApiData(
    @SerializedName("threads") val threads: List<MessageThreadDto> = emptyList(),
    @SerializedName("messages") val messages: List<MessageDto> = emptyList(),
)

data class MessageThreadDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("last_message") val lastMessage: String? = null,
    @SerializedName("last_message_at") val lastMessageAt: String? = null,
    @SerializedName("unread_count") val unreadCount: Int? = null,
)

data class MessageDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("text") val text: String? = null,
    @SerializedName("from") val from: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
)

// Send message

data class SendMessageRequest(
    @SerializedName("text") val text: String,
)

data class SendMessageResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: MessageDto? = null,
    @SerializedName("errors") val errors: Map<String, List<String>>? = null,
)
