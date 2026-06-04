package com.mtoanng.datastream.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mtoanng.datastream.data.dto.ChatMessage
import com.mtoanng.datastream.databinding.ItemChatUserBinding
import com.mtoanng.datastream.databinding.ItemChatModelBinding

private const val VIEW_TYPE_USER  = 0
private const val VIEW_TYPE_MODEL = 1

/**
 * RecyclerView adapter cho màn hình Chat.
 * Dùng 2 layout khác nhau: user (bên phải) và model/AI (bên trái).
 */
class ChatAdapter : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).isUser) VIEW_TYPE_USER else VIEW_TYPE_MODEL

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_USER -> UserViewHolder(
                ItemChatUserBinding.inflate(inflater, parent, false)
            )
            else -> ModelViewHolder(
                ItemChatModelBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UserViewHolder  -> holder.bind(getItem(position))
            is ModelViewHolder -> holder.bind(getItem(position))
        }
    }

    // ── ViewHolders ───────────────────────────────────────────────────────────

    class UserViewHolder(
        private val binding: ItemChatUserBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: ChatMessage) {
            binding.tvMessage.text = msg.text
        }
    }

    class ModelViewHolder(
        private val binding: ItemChatModelBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: ChatMessage) {
            binding.tvMessage.text = msg.text
            // Đổi màu nếu là tin lỗi
            binding.tvMessage.alpha = if (msg.isError) 0.6f else 1.0f
        }
    }

    // ── DiffCallback ──────────────────────────────────────────────────────────

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(old: ChatMessage, new: ChatMessage) =
                old.timestampMs == new.timestampMs && old.role == new.role

            override fun areContentsTheSame(old: ChatMessage, new: ChatMessage) =
                old == new
        }
    }
}
