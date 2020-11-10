package com.example.neighbourschatapp

import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_row_from.view.*
import kotlinx.android.synthetic.main.chat_row_to.view.*
import java.text.SimpleDateFormat
import java.util.*

class ChatItemTo (val chatMessage: ChatMessage, val user: User): Item <ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tv_to_row.text = chatMessage.text
        viewHolder.itemView.tv_message_date_to.text = convertLongToTime(chatMessage.timeStamp)
        val uri = user.userImageUrl
        val targetImageView = viewHolder.itemView.iv_chat_to_row
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_row_to
    }
    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("MM/dd HH:mm", Locale.UK)
        return format.format(date)
    }
}