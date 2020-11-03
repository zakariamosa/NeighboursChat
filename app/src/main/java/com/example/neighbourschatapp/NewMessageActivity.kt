package com.example.neighbourschatapp

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder

class NewMessageActivity : AppCompatActivity() {

    lateinit var rcvUsers: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title = "Start a new chat-conversation"

        rcvUsers = findViewById(R.id.recycler_view_users)
        val adapter = GroupAdapter <ViewHolder>()
        rcvUsers.adapter = adapter


        fetchUsers()
    }
    //Denna funktion laddar alla registrerade användare i en recyclerview i realtid, men laddar listan två gånger
    private fun fetchUsers() {

        val db = FirebaseFirestore.getInstance()
        val adapter = GroupAdapter <ViewHolder>()

        val itemRef = db.collection("users")

        itemRef.addSnapshotListener {snapshot, e ->
            if (snapshot != null) {

                for (document in snapshot.documents) {

                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        adapter.add(UserItem(user))
                    }
            }
        }
            adapter.setOnItemClickListener { item, view ->
                val userItem = item as UserItem
                val intent = Intent(view.context, ChatLogActivity::class.java)
                intent.putExtra("username", userItem.user.userName)
                startActivity(intent)
                finish()

            }
            rcvUsers.adapter = adapter
        }
    }
}