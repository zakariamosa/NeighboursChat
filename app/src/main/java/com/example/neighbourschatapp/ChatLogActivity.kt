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
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
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
        managetoken()

        btnSendChatLog.setOnClickListener {
            //Log.d(TAG, "Try to send message....")
            performSendMessage()
        }
    }

    private fun managetoken() {
        val db=FirebaseFirestore.getInstance()
        FirebaseService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            FirebaseService.token = it.token

            db.collection("users").document(FirebaseAuth.getInstance().uid!!).update("token",it.token)
        }
    }

    private fun initChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT < 26) {
            return
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)

        notificationManager.createNotificationChannel(channel)
    }

    private fun startTripNotification(notificationTitle:String, notificationtEXT:String,token:String) {

        val NOTIFICATION_CHANNEL_ID=token//"abcdefg123456"
        val NOTIFICATION_CHANNEL_NAME=token
        initChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME)

        val pendingIntent = PendingIntent.getActivity(this, 0, Intent(), 0)
        val     notification = NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID)
                .setContentTitle(notificationTitle)
                .setContentText(notificationtEXT)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher))
        notification.setContentIntent(pendingIntent)
        val notificationManager = this?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(token,0, notification.build())
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

        /*var cusers= mutableListOf<User>()
        db.collection("users").whereEqualTo("userId",fromId).get()
            .addOnSuccessListener {
                for (document in it.documents){
                    cusers.add(document.toObject(User::class.java)!!)
                    startTripNotification(user.userName,text,cusers[0].token)
                }
            }*/
        startTripNotification(user.userName,text,user.token)



   }
}