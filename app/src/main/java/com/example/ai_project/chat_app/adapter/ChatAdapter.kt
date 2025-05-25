package com.example.ai_project.chat_app.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ai_project.R
import com.example.ai_project.databinding.ItemMessageBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ChatAdapter : ListAdapter<ChatMessage, ChatAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val message = getItem(position)
//        holder.binding.apply {
//            isUser = message.isUser
//            messageText.text = message.text
//            timeText.text = SimpleDateFormat("hh:mm a", Locale.getDefault())
//                .format(message.timestamp)
//            executePendingBindings()
//        }
//    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = getItem(position)
        holder.binding.apply {
            isUser = message.isUser
            isTyping = message.isTyping
            messageText.text = if (message.isTyping) "" else message.text
            timeText.text = if (message.isTyping) "" else
                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(message.timestamp)
            executePendingBindings()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage) =
            oldItem == newItem
    }
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val isTyping: Boolean = false,
    val timestamp: Date = Date()
)