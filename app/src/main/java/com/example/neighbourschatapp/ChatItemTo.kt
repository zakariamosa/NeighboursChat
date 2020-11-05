package com.example.neighbourschatapp

import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_row_to.view.*

class ChatItemTo (val text: String, val user: User): Item <ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tv_to_row.text = text
        val uri = user.userImageUrl
        val targetImageView = viewHolder.itemView.iv_chat_to_row
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_row_to
    }
}