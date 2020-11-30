package com.example.neighbourschatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.text.set
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.fragment_register.*

class SettingsActivity : AppCompatActivity() {

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
            //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
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
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser
                ?.delete()
                ?.addOnSuccessListener(this) {
                    Toast.makeText(this, "Your account was successfully deleted " +
                            "and will be removed from database as soon as possible",
                        Toast.LENGTH_SHORT).show()
                    val waitForToast: CountDownTimer = object : CountDownTimer (2000, 1000) {
                        override fun onFinish() {
                            val intent = Intent(this@SettingsActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                        override fun onTick(millisUntilFinished: Long) {}
                    }
                    waitForToast.start()
                }
            ?.addOnFailureListener(this) {
                Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show()
            }
    }
}