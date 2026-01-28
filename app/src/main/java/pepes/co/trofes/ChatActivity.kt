package pepes.co.trofes

import android.os.Bundle
import android.text.format.DateFormat
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatActivity : AppCompatActivity() {

    private lateinit var adapter: ChatMessageAdapter
    private lateinit var lm: LinearLayoutManager
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        val fallback = getString(R.string.trofes_bot)
        val title = intent.getStringExtra(EXTRA_THREAD_NAME).orEmpty().ifBlank { fallback }
        findViewById<TextView>(R.id.tvChatTitle).text = title

        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }

        setupMessages()
        setupInput()

        seedInitialMessages(title)
    }

    private fun setupMessages() {
        val rv = findViewById<RecyclerView>(R.id.rvMessages)
        adapter = ChatMessageAdapter()

        lm = LinearLayoutManager(this).apply {
            stackFromEnd = true // kunci: chat mulai dari bawah
        }

        rv.layoutManager = lm
        rv.adapter = adapter

        // kecilin efek glow biar mirip aplikasi chat
        rv.overScrollMode = android.view.View.OVER_SCROLL_NEVER
    }

    private fun setupInput() {
        val et = findViewById<EditText>(R.id.etMessage)
        val btn = findViewById<ImageButton>(R.id.btnSend)

        fun send() {
            val text = et.text?.toString().orEmpty().trim()
            if (text.isBlank()) return

            messages.add(ChatMessage(id = "u_${messages.size}", text = text, timeText = nowTime(), fromUser = true))
            adapter.submitList(messages.toList())
            scrollToBottom()
            et.setText("")

            // simple bot response (dummy)
            messages.add(
                ChatMessage(
                    id = "b_${messages.size}",
                    text = "Oke, aku catat: \"$text\"",
                    timeText = nowTime(),
                    fromUser = false,
                )
            )
            adapter.submitList(messages.toList())
            scrollToBottom()
        }

        btn.setOnClickListener { send() }

        et.setOnEditorActionListener { _, actionId, event ->
            val isSend = actionId == EditorInfo.IME_ACTION_SEND
            val isEnter = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
            if (isSend || isEnter) {
                send()
                true
            } else {
                false
            }
        }
    }

    private fun seedInitialMessages(title: String) {
        // mirip screenshot: bot menyapa + quick replies
        val time = nowTime()
        messages.clear()

        messages.add(ChatMessage("b0", "Halo! Aku $title.", time, fromUser = false))
        messages.add(ChatMessage("b1", "Pilih salah satu atau ketik pesan ya.", time, fromUser = false))

        // quick replies sebagai pesan user (biar kelihatan seperti chips di gambar)
        messages.add(ChatMessage("u0", "Masukkan", time, fromUser = true))
        messages.add(ChatMessage("u1", "Komplain Resep", time, fromUser = true))
        messages.add(ChatMessage("u2", "Masalah Teknis", time, fromUser = true))

        adapter.submitList(messages.toList())
        scrollToBottom()
    }

    private fun scrollToBottom() {
        val rv = findViewById<RecyclerView>(R.id.rvMessages)
        rv.post {
            val last = adapter.itemCount - 1
            if (last >= 0) {
                rv.scrollToPosition(last)
            }
        }
    }

    private fun nowTime(): String {
        return DateFormat.format("h:mm a", System.currentTimeMillis()).toString().lowercase()
    }

    companion object {
        const val EXTRA_THREAD_NAME = "extra_thread_name"
    }
}
