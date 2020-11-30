package com.example.neighbourschatapp

import android.content.Intent
import android.content.pm.PackageManager
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder

class BlockUserActivity : AppCompatActivity() {

    lateinit var rcvUsers: RecyclerView
    private val REQUEST_LOCATION = 1
    lateinit var locationProvider: FusedLocationProviderClient
    var locationRequest: LocationRequest? = null
    lateinit var locationCallback: LocationCallback
    private var currentuserlat: Double = 0.0
    private var currentuserlong: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block_user)

        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        blocklista.clear()
        val itemRef =
            db.collection("BlockList").document(currentUser!!.uid).collection("UserBlockedList")
        itemRef.addSnapshotListener() { snapshot, e ->
            if (snapshot != null) {
                for (document in snapshot.documents) {
                    val settingblockuser = document.toObject(User::class.java)
                    if (settingblockuser != null) {
                        blocklista.add(settingblockuser)
                    }
                }
            }
        }
        rcvUsers = findViewById(R.id.rcv_block_user)


        val adapter = GroupAdapter <ViewHolder>()
        rcvUsers.adapter = adapter

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

        val backButtonBlockUser: ImageView = findViewById(R.id.iv_back_button_block_user_toolbar)
        backButtonBlockUser.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        val homeButtonBlockUser: ImageView = findViewById(R.id.iv_home_button_block_user_toolbar)
        homeButtonBlockUser.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

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

                                if (distancefrommeinkm<it.result.data?.get("locationDistance").toString().toDouble()){

                                    adapter.add(BlockUserItem(user))
                                }

                            }
                        }
                    }
                }
            }
            adapter.setOnItemClickListener { item, view ->
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