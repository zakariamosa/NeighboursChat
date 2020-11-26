package com.example.neighbourschatapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.squareup.picasso.Picasso
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
    private var lastMessage:String=""

    val TAG = "ChatLogActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        val backButtonToolbar: ImageView = findViewById(R.id.iv_back_button_chat_log_toolbar)
        val chatPartnerNameToolbar: TextView = findViewById(R.id.tv_chat_partner_name_chat_logtoolbar)

        val db=FirebaseFirestore.getInstance()
        FirebaseService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            FirebaseService.token = it.token

            db.collection("users").document(FirebaseAuth.getInstance().uid!!).update("token",it.token)
        }
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)


        toUser = intent.getParcelableExtra<User>("username")
        chatPartnerNameToolbar.text = toUser?.userName
        rcvChatLog = findViewById(R.id.recycler_view_chat_log)
        val btnSendChatLog: Button = findViewById(R.id.btn_send_chat_log)
        rcvChatLog.adapter = adapter

        listenForMessages()

        btnSendChatLog.setOnClickListener {
            //Log.d(TAG, "Try to send message....")
            performSendMessage()
            //startTripNotification()
            db.collection("users").whereEqualTo("userId",FirebaseAuth.getInstance().uid!!).get()
                .addOnSuccessListener {
                    for (document in it.documents){
                        val title = document.toObject(User::class.java)?.userName
                        val message = lastMessage
                        val recipientToken = toUser!!.token
                        if(title!!.isNotEmpty() && message.isNotEmpty() && recipientToken.isNotEmpty()) {
                            PushNotification(
                                NotificationData(title, message),
                                recipientToken
                            ).also {
                                sendNotification(it)
                            }
                        }
                    }
                }

        }
        backButtonToolbar.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
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
/*
    private fun initChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT < 26) {
            return
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)

        notificationManager.createNotificationChannel(channel)
    }

    private fun startTripNotification() {

        val NOTIFICATION_CHANNEL_ID="abcdefg123456"
        val NOTIFICATION_CHANNEL_NAME="developersgroup"
        initChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME)

        val pendingIntent = PendingIntent.getActivity(this, 0, Intent(), 0)
        val     notification = NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID)
                .setContentTitle("test notification title")
                .setContentText("test notification text")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher))
        notification.setContentIntent(pendingIntent)
        val notificationManager = this?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notification.build())
    }
*/
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
                    lastMessage=etChatLog.text.toString()
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