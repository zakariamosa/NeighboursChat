package com.example.neighbourschatapp

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder

class NewMessageActivity : AppCompatActivity() {

    lateinit var rcvUsers: RecyclerView
    private val REQUEST_LOCATION = 1
    lateinit var locationProvider: FusedLocationProviderClient
    var locationRequest : LocationRequest? = null
    lateinit var locationCallback: LocationCallback
    private var currentuserlat:Double=0.0
    private var currentuserlong:Double=0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title = "Start a new chat-conversation"

        rcvUsers = findViewById(R.id.recycler_view_users)
        val adapter = GroupAdapter <ViewHolder>()
        rcvUsers.adapter = adapter


        //currentUserSettings=loadUserSettings()
        locationProvider = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                for(location in locationResult.locations ) {
                    Log.d("!!!", "lat: ${location.latitude} lng: ${location.longitude}")
                }
            }
        }
        if( ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.d("!!!", "no permission")
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION)
        } else {
            locationProvider.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentuserlat = location.latitude
                    currentuserlong = location.longitude
                    Log.d("!!!", "last location lat: $currentuserlat, lng: $currentuserlong")
                    fetchUsers()
                    //will save this in user table

                }
            }
        }
        locationRequest = creatLocationRequest()
    }
    //Denna funktion laddar alla registrerade användare i en recyclerview i realtid, men laddar listan två gånger
    private fun fetchUsers() {

        val db = FirebaseFirestore.getInstance()
        val adapter = GroupAdapter <ViewHolder>()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val itemRef = db.collection("users")

        itemRef.addSnapshotListener {snapshot, e ->
            if (snapshot != null) {

                for (document in snapshot.documents) {

                    val user = document.toObject(User::class.java)
                    if (user != null && currentUser?.uid != user.userId) {
                        Log.d("!!!!", "$currentuserlat and $currentuserlong")
                        val itemRef = db.collection("Settings").document(currentUser!!.uid).get()
                        itemRef.addOnCompleteListener(){
                            if (it.isSuccessful){
                                val distancefrommeinkm=distance(currentuserlat,currentuserlong,user.lastLocationLat,user.lastLocationLong)
                                //Toast.makeText(this,distancefrommeinkm.toString() , Toast.LENGTH_SHORT).show()
                                if (distancefrommeinkm<it.result.data?.get("locationDistance").toString().toDouble()){
                                    //Toast.makeText(this,it.result.data?.get("locationDistance").toString() , Toast.LENGTH_SHORT).show()
                                    var showuser:Boolean=true
                                    db.collection("BlockList").document(currentUser.uid).collection("UserBlockedList").whereEqualTo("userId",user.userId)
                                            .get()
                                            .addOnSuccessListener {
                                                if (it.documents.size>0){
                                                    for (document in it.documents){
                                                        //do not show user when I block this user
                                                        showuser=false
                                                    }
                                                }
                                                db.collection("BlockList").document(user.userId).collection("UserBlockedList").whereEqualTo("userId",currentUser.uid)
                                                        .get()
                                                        .addOnSuccessListener {
                                                            if (it.documents.size>0) {
                                                                for (document in it.documents) {
                                                                    //do not show user when He/She blocked Me
                                                                    showuser = false

                                                                }
                                                            }
                                                            if (showuser){
                                                                adapter.add(UserItem(user))
                                                            }
                                                        }
                                            }

                                }

                            }
                        }
                    }
            }
        }
            adapter.setOnItemClickListener { item, view ->
                val userItem = item as UserItem
                val intent = Intent(view.context, ChatLogActivity::class.java)
                intent.putExtra("username", userItem.user)
                startActivity(intent)
                finish()

            }
            rcvUsers.adapter = adapter
        }
    }

    override fun onResume() {
        super.onResume()

        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()

        stopLocationUpdates()
    }

    fun startLocationUpdates() {
        if( ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
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

    //Here getting distance in kilometers (km)
    private fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val theta = lon1 - lon2
        var dist = (Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + (Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta))))
        dist = Math.acos(dist)
        dist = rad2deg(dist)
        dist = dist * 60 * 1.1515
        return dist
    }

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }
}