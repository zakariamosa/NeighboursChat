package com.example.neighbourschatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_set_distance.*

class SetDistanceActivity : AppCompatActivity() {

    var locationDistance: Int? = null
    lateinit var ld: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_distance)

        ld = findViewById(R.id.location_seekBar)
        loadUserSettings()

        ld.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, i: Int, b: Boolean
            ) {
                tv_location_distance.text = "$i kilometers"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                locationDistance = ld.progress

            }
        })

        val btnSaveSettings= findViewById<Button>(R.id.buttonSaveNeighbourLocationDistance)
        btnSaveSettings.setOnClickListener(){
            saveSettings(locationDistance.toString().toInt())
        }
    }

    private fun loadUserSettings() {

        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val itemRef = db.collection("Settings").document(currentUser!!.uid).get()
        itemRef.addOnCompleteListener(){
            if (it.isSuccessful){
                ld.setProgress(it.result.data?.get("locationDistance").toString().toInt())
                locationDistance = it.result.data?.get("locationDistance").toString().toInt()
            }
        }
        Log.d("!!!", locationDistance.toString())

    }

    private fun saveSettings(locationDistance:Int) {
        val userId = FirebaseAuth.getInstance().uid ?: ""
        val db = FirebaseFirestore.getInstance()
        val userSettings = UserSettings(locationDistance)

        db.collection("Settings").document(userId).set(userSettings)
                .addOnSuccessListener {

                    Toast.makeText(this, "Saved Successfully", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
    }
}