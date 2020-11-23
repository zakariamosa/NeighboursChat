package com.example.neighbourschatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.text.set
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.fragment_register.*

class SettingsActivity : AppCompatActivity() {
    lateinit var locationDistance:EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)




        val btnSignOut = findViewById<Button>(R.id.btn_sign_out)
        btnSignOut.setOnClickListener {
            signOut()
        }

        val btnDeleteAccount = findViewById<Button>(R.id.btn_delete_account)
        btnDeleteAccount.setOnClickListener {

        }


        val buttonNeighbourDistanceSetting=findViewById<Button>(R.id.buttonNeighbourDistanceSetting)
        buttonNeighbourDistanceSetting.setOnClickListener(){
            callNeighbourDistanceSetting()

        }

        val buttonBlockList=findViewById<Button>(R.id.buttonBlock)
        buttonBlockList.setOnClickListener(){
            val db = FirebaseFirestore.getInstance()
            val currentUser = FirebaseAuth.getInstance().currentUser
            blocklista.clear()
            val itemRef =db.collection("BlockList").document(currentUser!!.uid).collection("UserBlockedList")
            itemRef.addSnapshotListener(){snapshot,e->
                if (snapshot!=null){
                    for (document in snapshot.documents){
                        val settingblockuser =document.toObject(User::class.java)
                        if (settingblockuser!=null){
                            blocklista.add(settingblockuser)
                        }
                    }

                    callUserBlockListSetting()
                }

            }

        }


        supportActionBar?.title = "Settings"
    }

    private fun callUserBlockListSetting() {

        container.visibility = View.VISIBLE
        toggleVisibility()

        val blockListSettingFragment =  fragment_block_user.newInstance("","")

        val transaction = supportFragmentManager.beginTransaction()

        transaction.replace(R.id.container, blockListSettingFragment, "blockuserlist" )

        transaction.commit()
    }

    private fun callNeighbourDistanceSetting() {

        container.visibility = View.VISIBLE
        toggleVisibility()

        val neighbourDistanceSettingFragment =  fragment_neighbourdistance.newInstance("","")

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, neighbourDistanceSettingFragment, "neighbourdistance" )

        transaction.commit()

    }





    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun toggleVisibility() {
        if (container.visibility == View.VISIBLE) {
            btn_sign_out.visibility = View.INVISIBLE
            buttonBlock.visibility = View.INVISIBLE
            buttonNeighbourDistanceSetting.visibility = View.INVISIBLE
            btn_delete_account.visibility = View.INVISIBLE
        } else {
            btn_sign_out.visibility = View.VISIBLE
            buttonBlock.visibility = View.VISIBLE
            buttonNeighbourDistanceSetting.visibility = View.VISIBLE
            btn_delete_account.visibility = View.VISIBLE
        }
        return

    }

}