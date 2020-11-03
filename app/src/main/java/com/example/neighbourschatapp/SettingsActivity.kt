package com.example.neighbourschatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val btnSignOut = findViewById<TextView>(R.id.btn_sign_out)

        btnSignOut.setOnClickListener {
            signOut()
        }

        supportActionBar?.title = "Settings"
    }
    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}