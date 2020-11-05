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

    companion object {
        val TAG = "ChatLog"
    }
    val adapter = GroupAdapter <ViewHolder>()
    private lateinit var rcvChatLog: RecyclerView
    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        val user = intent.getParcelableExtra<User>("username")
        supportActionBar?.title = user?.userName
        rcvChatLog = findViewById(R.id.recycler_view_chat_log)
        val btnSendChatLog: Button = findViewById(R.id.btn_send_chat_log)

        rcvChatLog.adapter = adapter

        //setupDummyData()
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
        val db = FirebaseFirestore.getInstance()
        val query = db.collection("/messages").orderBy("timeStamp")
        listener = query.addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "listen:error", e)
                    return@addSnapshotListener
                }
                for (dc in snapshots!!.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val chatMessage = dc.document.toObject(ChatMessage::class.java);

                        if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                            adapter.add(ChatItemFrom(chatMessage.text))
                            Log.d(TAG, chatMessage.text)
                        }
                        else {
                            adapter.add(ChatItemTo(chatMessage.text))
                        }
                    }
                    else if (dc.type == DocumentChange.Type.MODIFIED) {
                        Log.d(TAG, "Modified")
                    }
                    else if (dc.type == DocumentChange.Type.REMOVED) {
                        Log.d(TAG, "Removed")
                    }
                }
            }
    }

    private fun performSendMessage() {
        val etChatLog: EditText = findViewById(R.id.et_chat_log)
        val messageId = UUID.randomUUID().toString()
        //val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val db = FirebaseFirestore.getInstance()
        val text = etChatLog.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>("username")
        val toId = user?.userId

        if (fromId == null)
            return

        val chatMessage = ChatMessage(db.collection("/messages").document(messageId).id, text,
            fromId, toId!!, System.currentTimeMillis()/1000)
            //reference.setValue(chatMessage)
                db.collection("/messages").document(messageId).set(chatMessage)
                .addOnSuccessListener {
                    Log.d(TAG, "saved our message")
                }
    }

    private fun setupDummyData() {
        val adapter = GroupAdapter <ViewHolder>()

        adapter.add(ChatItemFrom("Hej hej hej"))
        adapter.add(ChatItemTo("From message\n From message \n From message"))


        rcvChatLog.adapter = adapter
    }
}