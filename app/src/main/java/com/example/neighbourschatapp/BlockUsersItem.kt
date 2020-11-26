package com.example.neighbourschatapp

import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
        viewHolder.itemView.checkBoxSelecttoBlock.isChecked=when(isuserblocked(blocklista,user)){
            true->true
            false->false
        }
        viewHolder.itemView.checkBoxSelecttoBlock.setOnClickListener{
            val select=viewHolder.itemView.checkBoxSelecttoBlock.isChecked
            val userId = FirebaseAuth.getInstance().uid ?: ""
            val db = FirebaseFirestore.getInstance()
            val blockeduserid = user.userId
            when(select){
                true->{
                    blocklista.add(user)
                    val blkuser=BlockList(blocklista)

                    db.collection("BlockList").document(userId).collection("UserBlockedList").add(user)//.set(blkuser)
                            .addOnSuccessListener {

                                Toast.makeText(viewHolder.itemView.context, "you just blocked ${user.userName} he/she will not be able to chat with you!", Toast.LENGTH_SHORT).show()
                            }
                }
                false->{
                    db.collection("BlockList").document(userId).collection("UserBlockedList").whereEqualTo("userId",user.userId)
                            .get()
                            .addOnSuccessListener {
                                for (document in it.documents){
                                    val documentidforblockeduser=document.toObject(User::class.java)
                                    db.collection("BlockList").document(userId).collection("UserBlockedList").document(document.id).delete().addOnCompleteListener(){
                                        if (it.isSuccessful){
                                            blocklista.removeAll { u->u.userId==user.userId }
                                            Toast.makeText(viewHolder.itemView.context, "you just unblocked ${user.userName} he/she will be able to chat with you!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                }
            }
        }
    }

    private fun isuserblocked(blocklista: MutableList<User>, user: User): Boolean {
        if (blocklista.size>0){
            for (i in 0..blocklista.size-1){
                if (blocklista[i].userId==user.userId){
                    return true
                }
            }

        }

        return false
    }
}