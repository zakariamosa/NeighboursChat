package com.example.neighbourschatapp

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.DocumentsContract
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.Query
import com.google.firebase.firestore.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder


class ChatActivity : AppCompatActivity() {

    companion object {
        var currentUser: User? = null
    }
    val latestMessagesMap = HashMap <String, ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val rcvLatestChat: RecyclerView = findViewById(R.id.recycler_view_latest_chat)
        rcvLatestChat.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rcvLatestChat.adapter = adapter
        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ChatLogActivity::class.java)
            val row = item as LatestMessageChatRow
            intent.putExtra("username", row.chatPartnerUser)
            startActivity(intent)
        }

        fetchCurrentUser()
        listenForLatestMessages()


    }
    private fun refreshRecyclerView() {
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessageChatRow(it))
        }

    }

    private fun listenForLatestMessages() {

        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)?: return
                latestMessagesMap[snapshot.key!!] = chatMessage
                refreshRecyclerView()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)?: return
                latestMessagesMap[snapshot.key!!] = chatMessage
                refreshRecyclerView()


            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onCancelled(error: DatabaseError) {}
        })

        /*
        val fromId = FirebaseAuth.getInstance().uid
        val db = FirebaseFirestore.getInstance()
        val query = db.collection("/latest-messages").document("/$fromId")
            .addSnapshotListener {snapshot, e ->
               if (snapshot != null) {
                val chatMessage = snapshot.toObject(ChatMessage::class.java) ?: return@addSnapshotListener
                latestMessagesMap[chatMessage.toId] = chatMessage
                   adapter.clear()
                   latestMessagesMap?.values?.forEach {
                       adapter.add(LatestMessageChatRow(it))
                   }
                }
            }

         */
    }
    val adapter = GroupAdapter <ViewHolder>()

    private fun fetchCurrentUser() {

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val db = FirebaseFirestore.getInstance().collection("users").document(uid)
        db.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null) {
                    currentUser = documentSnapshot.toObject(User::class.java)
                    Log.d("!!!!", currentUser!!.userName)
                }
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_bar_nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}

