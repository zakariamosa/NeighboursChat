package com.example.neighbourschatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.text.set
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_register.*

class SettingsActivity : AppCompatActivity() {
    lateinit var locationDistance:EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)



        locationDistance=findViewById(R.id.editTextNumberLocationDistance)
        val btnSignOut = findViewById<TextView>(R.id.btn_sign_out)
        btnSignOut.setOnClickListener {
            signOut()
        }

        val btnSaveSettings=findViewById<Button>(R.id.buttonSaveSettings)
        btnSaveSettings.setOnClickListener(){
            saveSettings(locationDistance.text.toString().toInt())
        }

        loadUserSettings()

        supportActionBar?.title = "Settings"
    }

    private fun loadUserSettings() {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val itemRef = db.collection("Settings").document(currentUser!!.uid).get()
        itemRef.addOnCompleteListener(){
            if (it.isSuccessful){
                locationDistance.setText(it.result.data?.get("locationDistance").toString())

            }
        }
    }

    private fun saveSettings(locationDistance:Int) {
        val userId = FirebaseAuth.getInstance().uid ?: ""
        val db = FirebaseFirestore.getInstance()
        val userSettings = UserSettings(locationDistance)

        db.collection("Settings").document(userId).set(userSettings)
                .addOnSuccessListener {

                    val intent = Intent(this, NewMessageActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}