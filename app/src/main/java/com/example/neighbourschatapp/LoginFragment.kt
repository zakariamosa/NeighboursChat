package com.example.neighbourschatapp

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment: Fragment() {



    private val REQUEST_LOCATION = 1
    lateinit var locationProvider: FusedLocationProviderClient
    var locationRequest : LocationRequest? = null
    lateinit var locationCallback: LocationCallback
    private var currentuserlat:Double=0.0
    private var currentuserlong:Double=0.0
    lateinit var thisactivity: Activity
    @ExperimentalStdlibApi
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_login, container, false)

        val btnLogin: Button = view.findViewById(R.id.login_button_login)
        val tvGoToRegister: TextView = view.findViewById(R.id.go_to_register_textview)

        btnLogin.setOnClickListener {
            performLogin()
        }

        //Denna lyssnare skickar användaren till fragmentet för registrering
        tvGoToRegister.setOnClickListener {

            val transaction: FragmentTransaction = this.fragmentManager!!.beginTransaction()
            val frag: Fragment = RegisterFragment()
            transaction.replace(R.id.main_fragment_layout, frag)
            transaction.commit()

        }

        thisactivity= getActivity()!!
        locationProvider = LocationServices.getFusedLocationProviderClient(thisactivity)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                for(location in locationResult.locations ) {
                    Log.d("!!!", "lat: ${location.latitude} lng: ${location.longitude}")
                }
            }
        }
        if( ActivityCompat.checkSelfPermission(thisactivity.baseContext, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.d("!!!", "no permission")
            ActivityCompat.requestPermissions(thisactivity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION)
        } else {
            locationProvider.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentuserlat = location.latitude
                    currentuserlong = location.longitude
                    Log.d("!!!", "last location lat: $currentuserlat, lng: $currentuserlong")
                    //will save this in user table
                }
            }
        }
        locationRequest = creatLocationRequest()

        return view
    }
    //Denna funktion loggar in användaren om denna har ett existerande användarkonto.
    private fun performLogin() {
        val eMail = email_edittext_login.text.toString()
        val password = password_edittext_login.text.toString()

        if (eMail.isEmpty() || password.isEmpty()) {
            Toast.makeText(activity!!.applicationContext, "Please enter both e-mail and password", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(eMail, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                Log.d("Main", "user succefully logged in with uid ${it.result?.user?.uid}")
                updateuserlocation(it.result?.user?.uid)
                //fillBlockList()
                val theGeneralclass=General()
                theGeneralclass.getblockedUsers()
                val intent = Intent(this@LoginFragment.context, ChatActivity::class.java)
                //Denna rad rensar upp i tidigare aktiviteter
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(activity!!.applicationContext, "Failed to login user: ${it.message}", Toast.LENGTH_SHORT).show()
                Log.d("Main", "Failed to login user: ${it.message}")

            }
    }

 /*   private fun fillBlockList() {
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

            }

        }
    }*/

    private fun updateuserlocation(uid: String?) {

        val db=FirebaseFirestore.getInstance()

        db.collection("users").document(uid!!).update("lastLocationLat",currentuserlat,"lastLocationLong",currentuserlong)
    }

    //Denna delen hanterar location
    override fun onResume() {
        super.onResume()

        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()

        stopLocationUpdates()
    }

    fun startLocationUpdates() {
        if( ActivityCompat.checkSelfPermission(thisactivity.baseContext, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            locationProvider.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }

    }

    fun stopLocationUpdates() {
        locationProvider.removeLocationUpdates(locationCallback)
    }

    fun creatLocationRequest()  =
            LocationRequest.create().apply{
                interval = 2000
                fastestInterval = 1000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == REQUEST_LOCATION ) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                Log.d("!!!", "Permission granted")
                startLocationUpdates()
            } else {
                Log.d("!!!", "Permission denied")
            }
        }

    }
}