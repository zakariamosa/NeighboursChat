package com.example.neighbourschatapp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.ContactsContract
import android.provider.DocumentsContract
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.Query
import com.google.firebase.firestore.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ChatActivity : AppCompatActivity() {

    companion object {
        var currentUser: User? = null
    }
    lateinit var userImageToolBar: ImageView
    val latestMessagesMap = HashMap <String, ChatMessage>()
    var chatMessage = ChatMessage()
    val adapter = GroupAdapter <ViewHolder>()
    var fromId: String? = null
    var toId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        userImageToolBar = findViewById<ImageView>(R.id.iv_user_photo_toolbar)

        val openUserProfile: ImageView = findViewById(R.id.iv_user_photo_toolbar)
        val openSettings: ImageView = findViewById(R.id.iv_settings_toolbar)
        val openNewMessage: ImageView = findViewById(R.id.iv_new_message_toolbar)

        val rcvLatestChat: RecyclerView = findViewById(R.id.recycler_view_latest_chat)
        rcvLatestChat.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rcvLatestChat.adapter = adapter
        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ChatLogActivity::class.java)
            val row = item as LatestMessageChatRow
            fromId = FirebaseAuth.getInstance().uid
            toId = row.chatPartnerUser!!.userId
            intent.putExtra("username", row.chatPartnerUser)
            startActivity(intent)
            /*
            Om jag ändrar read till true direkt på klick så reagerar recycler view:n på det direkt och det blir en
            "glitch" i designen. Därför har jag satt ett delay på en halv sekund så att intent hinner genomföras
            innan värdet ändras i databasen.
             */
            object : CountDownTimer (500,1000){
                override fun onFinish() {
                    FirebaseDatabase.getInstance().getReference("latest-messages/$fromId/$toId/read").setValue(true)
                }
                override fun onTick(millisUntilFinished: Long) {}
            }.start()

        }

        fetchCurrentUser()
        listenForLatestMessages()

        openUserProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        openSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        openNewMessage.setOnClickListener {
            val intent = Intent(this, NewMessageActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }


    }
    private fun refreshRecyclerView() {
        val list = ArrayList<ChatMessage>()
        adapter.clear()
        latestMessagesMap.values.forEach {
            list.add(it)
        }
        list.sortByDescending { it.timeStamp }
        for (message in list) {
            adapter.add(0, LatestMessageChatRow(message))
        }

    }


    private fun listenForLatestMessages() {

        val theGeneralclass=General()
        val blocklistfilledup=theGeneralclass.getblockedUsers()
        if (!blocklistfilledup){
            Thread.sleep(1_00)
        }


        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                chatMessage = snapshot.getValue(ChatMessage::class.java)?: return
                if(blocklistaMeAndThem.any { bl->bl.userId==chatMessage.toId }){return}
                latestMessagesMap[snapshot.key!!] = chatMessage
                refreshRecyclerView()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                chatMessage = snapshot.getValue(ChatMessage::class.java)?: return
                if(blocklistaMeAndThem.any { bl->bl.userId==chatMessage.toId }){return}
                latestMessagesMap[snapshot.key!!] = chatMessage
                refreshRecyclerView()


            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onCancelled(error: DatabaseError) {}
        })

    }

    private fun fetchCurrentUser() {

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val db = FirebaseFirestore.getInstance().collection("users").document(uid)
        db.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null) {
                    currentUser = documentSnapshot.toObject(User::class.java)
                    Log.d("!!!!", currentUser!!.userName)
                    Picasso.get().load(currentUser!!.userImageUrl).into(userImageToolBar)
                }
            }
    }
}


