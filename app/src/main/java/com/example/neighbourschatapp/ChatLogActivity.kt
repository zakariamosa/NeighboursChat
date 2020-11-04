package com.example.neighbourschatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import java.util.*

class ChatLogActivity : AppCompatActivity() {

    companion object {
        val TAG = "ChatLog"
    }

    private lateinit var rcvChatLog: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        val user = intent.getParcelableExtra<User>("username")
        supportActionBar?.title = user?.userName
        rcvChatLog = findViewById(R.id.recycler_view_chat_log)
        val btnSendChatLog: Button = findViewById(R.id.btn_send_chat_log)

        //setupDummyData()
        listenForMessages()

        btnSendChatLog.setOnClickListener {
            Log.d(TAG, "Try to send message....")
            performSendMessage()
        }
    }
    private fun listenForMessages() {
        val db = FirebaseFirestore.getInstance()
        db.collection("messages/").addSnapshotListener { snapshot, e ->
            if (snapshot != null) {
                for (document in snapshot.documents) {
                    val chatMessage = document.toObject(ChatMessage::class.java)
                    Log.d(TAG, chatMessage!!.text)
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