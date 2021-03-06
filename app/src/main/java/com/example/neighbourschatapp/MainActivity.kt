package com.example.neighbourschatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

var blocklista= mutableListOf<User>()
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Här kontrolleras om användaren är inloggad.
        verifyUserIsLoggedIn()

        //Här laddar jag LoginFragment så fort mainactivity startas.
        loadFragment(LoginFragment())
    }
    //Denna funktion skapar fragmentet i mainActivity
    private fun loadFragment(fragment: Fragment) {

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_fragment_layout, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
    private fun verifyUserIsLoggedIn() {
        val userId = FirebaseAuth.getInstance().uid

        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            blocklista.clear()
            val itemRef =db.collection("BlockList").document(userId).collection("UserBlockedList")
            itemRef.addSnapshotListener(){snapshot,e->
                if (snapshot!=null){
                    for (document in snapshot.documents){
                        val settingblockuser =document.toObject(User::class.java)
                        if (settingblockuser!=null){
                            blocklista.add(settingblockuser)
                        }
                    }
                }
                val intent = Intent(this, ChatActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }

        }
    }
}