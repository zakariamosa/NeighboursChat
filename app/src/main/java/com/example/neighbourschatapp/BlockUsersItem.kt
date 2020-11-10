package com.example.neighbourschatapp

import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_block_user_row.view.*



class BlockUserItem(val user: User) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.activity_block_user_row
    }
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tv_block_user_name.text = user.userName
        Picasso.get().load(user.userImageUrl).into(viewHolder.itemView.iv_block_user_photo)
        viewHolder.itemView.checkBoxSelecttoBlock.isChecked=false
    }

}