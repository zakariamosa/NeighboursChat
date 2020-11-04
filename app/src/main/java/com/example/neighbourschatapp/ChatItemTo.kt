package com.example.neighbourschatapp

import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_row_to.view.*

class ChatItemTo (val text: String): Item <ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tv_to_row.text = text
    }

    override fun getLayout(): Int {
        return R.layout.chat_row_to
    }
}