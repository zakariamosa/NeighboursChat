package com.example.neighbourschatapp

import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_profile.view.*
import kotlinx.android.synthetic.main.chat_row_from.view.*

class ChatItemFrom(val text: String): Item <ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tv_from_row.text = text
    }

    override fun getLayout(): Int {
        return R.layout.chat_row_from
    }
}