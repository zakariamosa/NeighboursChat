package com.example.neighbourschatapp

import android.net.nsd.NsdManager
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_row_from.view.*
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessageChatRow(val chatMessage: ChatMessage): Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }
    var chatPartnerUser: User? = null

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tv_chat_latest_message.text = chatMessage.text

        val chatPartnerId: String
        if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
            chatPartnerId = chatMessage.toId
        } else {
            chatPartnerId = chatMessage.fromId
        }

        //kolla på att ändra detta till något som liknar ListenerForSingleEventValue ist. Detta känns onödigt långt.
        val db = FirebaseFirestore.getInstance()
        val query = db.collection("users")
        query.addSnapshotListener { p0, e ->
            if (p0 != null) {
                for (dc in p0.documentChanges) {
                    val user = dc.document.toObject(User::class.java)
                    if (user.userId == chatPartnerId) {
                        chatPartnerUser = user
                        var targetImageView = viewHolder.itemView.iv_latest_message
                        Picasso.get().load(user.userImageUrl).into(targetImageView)
                        val chatPartnerUsername = user.userName
                        if (dc.type == DocumentChange.Type.ADDED) {
                            targetImageView = viewHolder.itemView.iv_latest_message
                            Picasso.get().load(user.userImageUrl).into(targetImageView)
                            viewHolder.itemView.tv_username_latest_message.text =
                                chatPartnerUsername
                        } else if (dc.type == DocumentChange.Type.MODIFIED) {
                            viewHolder.itemView.tv_username_latest_message.text =
                                chatPartnerUsername
                        }}
                    }
                }

            }
        }
}


