package com.example.neighbourschatapp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [fragment_neighbourdistance.newInstance] factory method to
 * create an instance of this fragment.
 */
class fragment_neighbourdistance : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var locationDistance:EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val myview= inflater.inflate(R.layout.fragment_neighbourdistance, container, false)

        locationDistance=myview.findViewById(R.id.editTextNumberNeighbourLocationDistance)
        loadUserSettings()
        val btnSaveSettings=myview.findViewById<Button>(R.id.buttonSaveNeighbourLocationDistance)
        btnSaveSettings.setOnClickListener(){
            if (locationDistance.text.toString().toInt() > 10) {
                Toast.makeText(this@fragment_neighbourdistance.context, "Maximum distance allowed is 10 km", Toast.LENGTH_SHORT).show()
            }
            else {
                saveSettings(locationDistance.text.toString().toInt())
            }

        }
        return myview
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

                Toast.makeText(this@fragment_neighbourdistance.context, "Saved Successfully", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@fragment_neighbourdistance.context, SettingsActivity::class.java)
                startActivity(intent)
            }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment fragment_neighbourdistance.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                fragment_neighbourdistance().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}