package com.example.neighbourschatapp

import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

class ChatItemFrom: Item <ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
    }

    override fun getLayout(): Int {
        return R.layout.chat_row_from
    }
}