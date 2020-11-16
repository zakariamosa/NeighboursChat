package com.example.neighbourschatapp

import android.content.Context
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
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.sql.Timestamp
import java.util.*

const val TOPIC = "/topics/myTopic2"

class ChatLogActivity : AppCompatActivity() {

    val adapter = GroupAdapter <ViewHolder>()
    private lateinit var rcvChatLog: RecyclerView
    private var listener: ListenerRegistration? = null
    private var toUser: User? = null

    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        val db=FirebaseFirestore.getInstance()
        FirebaseService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            FirebaseService.token = it.token

            db.collection("users").document(FirebaseAuth.getInstance().uid!!).update("token",it.token)
        }
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)


        toUser = intent.getParcelableExtra<User>("username")
        supportActionBar?.title = toUser?.userName
        rcvChatLog = findViewById(R.id.recycler_view_chat_log)
        val btnSendChatLog: Button = findViewById(R.id.btn_send_chat_log)
        rcvChatLog.adapter = adapter

        listenForMessages()

        btnSendChatLog.setOnClickListener {
            //Log.d(TAG, "Try to send message....")
            performSendMessage()
            val title = "my message title"
            val message = "this is a notification from ${FirebaseAuth.getInstance().uid} to ${toUser!!.userId}"
            val recipientToken = toUser!!.token
            if(title.isNotEmpty() && message.isNotEmpty() && recipientToken.isNotEmpty()) {
                PushNotification(
                        NotificationData(title, message),
                        recipientToken
                ).also {
                    sendNotification(it)
                }
            }
        }
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful) {
                Log.d(TAG, "Response: ${Gson().toJson(response)}")
            } else {
                Log.e(TAG, response.errorBody().toString())
            }
        } catch(e: Exception) {
            Log.e(TAG, e.toString())
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
                            adapter.add(ChatItemFrom(chatMessage, currentUser!!))
                            rcvChatLog.scrollToPosition(adapter.itemCount -1)
                        }
                        else {

                            adapter.add(ChatItemTo(chatMessage, toUser!!))
                            rcvChatLog.scrollToPosition(adapter.itemCount -1)
                        }
                    }
                    else if (dc.type == DocumentChange.Type.MODIFIED) {
                        Log.d("!!!", "Modified")
                    }
                    else if (dc.type == DocumentChange.Type.REMOVED) {
                        Log.d("!!!", "Removed")
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
        fromId, toId, System.currentTimeMillis(), false)
        db.collection("user-messages").document("/$toId/$fromId/$messageId").set(chatMessageTo)
            .addOnSuccessListener {
                //Log.d(TAG,"saved to message")
            }
        val chatMessageFromTrue = ChatMessage(db.collection("user-messages")
            .document("/$fromId/$toId/$messageId").id, text,
            fromId, toId, System.currentTimeMillis(), true)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessageFromTrue)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessageFrom)

   }
}