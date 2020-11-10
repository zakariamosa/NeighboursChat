package com.example.neighbourschatapp

import android.util.Log
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_row_from.view.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ChatItemFrom(val chatMessage: ChatMessage, val user: User): Item <ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tv_from_row.text = chatMessage.text
        viewHolder.itemView.tv_message_date_from.text = convertLongToTime(chatMessage.timeStamp)
        val uri = user.userImageUrl
        val targetImageView = viewHolder.itemView.iv_chat_from_row
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_row_from
    }
    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("MM/dd HH:mm", Locale.UK)
        return format.format(date)
    }
}