package com.example.neighbourschatapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder


class ChatActivity : AppCompatActivity() {

    lateinit var rcvUsers: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        rcvUsers = findViewById(R.id.recycler_view_users)
        val adapter = GroupAdapter <ViewHolder>()

        rcvUsers.adapter = adapter


        fetchUsers()
    }
    //Denna funktion laddar alla registrerade användare i en recyclerview
    private fun fetchUsers() {

        val db = FirebaseFirestore.getInstance()
        val adapter = GroupAdapter <ViewHolder>()

        val itemRef = db.collection("users")

        itemRef.get().addOnSuccessListener {documentSnapshot ->
            for (document in documentSnapshot.documents) {

                val user = document.toObject(User::class.java)
                if (user != null) {
                    adapter.add(UserItem(user))
            }
        }
        /*
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

         */
                rcvUsers.adapter = adapter
            }
    }
}