package com.example.neighbourschatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder

class ChatActivity : AppCompatActivity() {

    lateinit var rcvNewMessages: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        rcvNewMessages = findViewById(R.id.recycler_view_users)
        val adapter = GroupAdapter <ViewHolder>()

        rcvNewMessages.adapter = adapter


        fetchUsers()
    }
    //Denna funktion laddar alla registrerade användare i en recyclerview
    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter <ViewHolder>()
                snapshot.children.forEach {
                    Log.d("NewMessage", it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        adapter.add(UserItem(user))
                    }
                }
                rcvNewMessages.adapter = adapter
            }
        })
    }

}