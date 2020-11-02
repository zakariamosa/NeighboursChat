package com.example.neighbourschatapp

import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.user_row.view.*

class UserItem(val user: User) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.user_row
    }
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tv_user_name.text = user.userName
        Picasso.get().load(user.userImageUrl).into(viewHolder.itemView.iv_user_photo)
    }

}