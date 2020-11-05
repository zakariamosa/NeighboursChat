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
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder


class ChatActivity : AppCompatActivity() {

    companion object {
        var currentUser: User? = null
    }
    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        fetchCurrentUser()

    }

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