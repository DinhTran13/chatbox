package com.example.educhatbot


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(private val messageList: List<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Đã sửa thành R (viết hoa)
        val tvUserMessage: TextView = itemView.findViewById(R.id.tvUserMessage)
        val tvAiMessage: TextView = itemView.findViewById(R.id.tvAiMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        // Nạp file item_message.xml
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messageList[position]

        if (message.isSentByUser) {
            // Nếu là User: Hiện màu xanh, ẩn màu xám
            holder.tvUserMessage.visibility = View.VISIBLE
            holder.tvAiMessage.visibility = View.GONE
            holder.tvUserMessage.text = message.text
        } else {
            // Nếu là AI: Hiện màu xám, ẩn màu xanh
            holder.tvAiMessage.visibility = View.VISIBLE
            holder.tvUserMessage.visibility = View.GONE
            holder.tvAiMessage.text = message.text
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }
}