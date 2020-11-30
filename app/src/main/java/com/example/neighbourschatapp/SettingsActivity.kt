package com.example.neighbourschatapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsActivity : AppCompatActivity() {

    val userId = FirebaseAuth.getInstance().uid ?: ""
    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val btnSignOut = findViewById<Button>(R.id.btn_sign_out)
        btnSignOut.setOnClickListener {
            val builder = AlertDialog.Builder(this@SettingsActivity)
            builder.setMessage("Are you sure you want to sign out?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    signOut()
                    finish()
                }
                .setNegativeButton("No") { dialog, id ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }
        val btnDeleteAccount = findViewById<Button>(R.id.btn_delete_account)
        btnDeleteAccount.setOnClickListener {
            val builder = AlertDialog.Builder(this@SettingsActivity)
            builder.setMessage("Are you sure you want to delete your account?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    deleteAccount()
                    finish()
                }
                .setNegativeButton("No") { dialog, od ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }
        val buttonNeighbourDistanceSetting =
            findViewById<Button>(R.id.buttonNeighbourDistanceSetting)
        buttonNeighbourDistanceSetting.setOnClickListener() {

            val intent = Intent(this, SetDistanceActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        val buttonBlockList = findViewById<Button>(R.id.buttonBlock)
        buttonBlockList.setOnClickListener() {

            val intent = Intent(this, BlockUserActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

        }
        val backButtonSettings : ImageView = findViewById(R.id.iv_back_button_settings_toolbar)
        backButtonSettings.setOnClickListener {

            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)

        }

    }
    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
    private fun deleteAccount() {
        val deletedUser = DeletedUser(userId)
        db.collection("deleted-users").document(userId).set(deletedUser)
        db.collection("users").document(userId).delete()
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()

    }

}