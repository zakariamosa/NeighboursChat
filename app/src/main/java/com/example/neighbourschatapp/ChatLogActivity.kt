package com.example.neighbourschatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import java.sql.Timestamp
import java.util.*

class ChatLogActivity : AppCompatActivity() {

    val adapter = GroupAdapter <ViewHolder>()
    private lateinit var rcvChatLog: RecyclerView
    private var listener: ListenerRegistration? = null
    private var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)


        toUser = intent.getParcelableExtra<User>("username")
        supportActionBar?.title = toUser?.userName
        rcvChatLog = findViewById(R.id.recycler_view_chat_log)
        val btnSendChatLog: Button = findViewById(R.id.btn_send_chat_log)
        rcvChatLog.adapter = adapter

        listenForMessages()

        btnSendChatLog.setOnClickListener {
            //Log.d(TAG, "Try to send message....")
            performSendMessage()
        }
    }

    override fun onStop() {
        super.onStop()
        listener!!.remove()
    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser!!.userId
        val db = FirebaseFirestore.getInstance()
        val query = db.collection("/user-messages/$fromId/$toId").orderBy("timeStamp")

        listener = query.addSnapshotListener { snapshots, e ->

                if (e != null) {
                    //Log.w(TAG, "listen:error", e)
                    return@addSnapshotListener
                }
                for (dc in snapshots!!.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val chatMessage = dc.document.toObject(ChatMessage::class.java)

                        if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                            val currentUser = ChatActivity.currentUser
                            adapter.add(ChatItemFrom(chatMessage.text, currentUser!!))
                            rcvChatLog.scrollToPosition(adapter.itemCount -1)
                        }
                        else {

                            adapter.add(ChatItemTo(chatMessage.text, toUser!!))
                            rcvChatLog.scrollToPosition(adapter.itemCount -1)
                        }
                    }
                    else if (dc.type == DocumentChange.Type.MODIFIED) {
                        //Log.d(TAG, "Modified")
                    }
                    else if (dc.type == DocumentChange.Type.REMOVED) {
                        //Log.d(TAG, "Removed")
                    }
                }
            }
    }

    private fun performSendMessage() {
        val etChatLog: EditText = findViewById(R.id.et_chat_log)
        val messageId = UUID.randomUUID().toString()
        val db = FirebaseFirestore.getInstance()
        val text = etChatLog.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>("username")
        val toId = user?.userId

        if (fromId == null)
            return

        val chatMessageFrom = ChatMessage(db.collection("user-messages")
            .document("/$fromId/$toId/$messageId").id, text,
            fromId, toId!!, System.currentTimeMillis(), false)
            db.collection("user-messages").document("/$fromId/$toId/$messageId").set(chatMessageFrom)
                .addOnSuccessListener {
                    //Log.d(TAG, "saved our message")
                    etChatLog.text.clear()
                    rcvChatLog.scrollToPosition(adapter.itemCount -1)
                }

        val chatMessageTo = ChatMessage(db.collection("user-messages")
            .document("/$toId/$fromId/$messageId").id, text,
        fromId, toId!!, System.currentTimeMillis(), false)
        db.collection("user-messages").document("/$toId/$fromId/$messageId").set(chatMessageTo)
            .addOnSuccessListener {
                //Log.d(TAG,"saved to message")
            }

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessageFrom)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessageFrom)

/*
        db.collection("latest-messages").document("/$fromId").set(chatMessageFrom)
            .addOnSuccessListener {
                //Log.d(TAG, "saved our message")
                etChatLog.text.clear()
                rcvChatLog.scrollToPosition(adapter.itemCount -1)
            }

        db.collection("latest-messages").document("/$toId").set(chatMessageFrom)
            .addOnSuccessListener {
                //Log.d(TAG,"saved to message")
            }

 */

   }
}