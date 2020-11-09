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




        val btnSignOut = findViewById<TextView>(R.id.btn_sign_out)
        btnSignOut.setOnClickListener {
            signOut()
        }





        val buttonNeighbourDistanceSetting=findViewById<Button>(R.id.buttonNeighbourDistanceSetting)
        buttonNeighbourDistanceSetting.setOnClickListener(){
            callNeighbourDistanceSetting()
        }


        supportActionBar?.title = "Settings"
    }

    private fun callNeighbourDistanceSetting() {

        val neighbourDistanceSettingFragment =  fragment_neighbourdistance.newInstance("","")

        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.container, neighbourDistanceSettingFragment, "neighbourdistance" )

        transaction.commit()
    }





    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}