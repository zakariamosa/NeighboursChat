package com.example.neighbourschatapp

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
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


        //fetchUsers()
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
                    //will save this in user table
                }
            }

        }

        locationRequest = creatLocationRequest()

        fetchUsers()
    }
    //Denna funktion laddar alla registrerade användare i en recyclerview i realtid, men laddar listan två gånger
    private fun fetchUsers() {

        val db = FirebaseFirestore.getInstance()
        val adapter = GroupAdapter <ViewHolder>()

        val itemRef = db.collection("users")

        itemRef.addSnapshotListener {snapshot, e ->
            if (snapshot != null) {

                for (document in snapshot.documents) {

                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        adapter.add(UserItem(user))
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
}