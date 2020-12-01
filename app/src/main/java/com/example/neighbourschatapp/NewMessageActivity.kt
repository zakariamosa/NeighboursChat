package com.example.neighbourschatapp

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.os.PersistableBundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlin.math.roundToInt

class NewMessageActivity : AppCompatActivity() {
    companion object {
        var currentuserlat:Double=0.0
        var currentuserlong:Double=0.0
    }

    lateinit var rcvUsers: RecyclerView
    private val REQUEST_LOCATION = 1
    lateinit var locationProvider: FusedLocationProviderClient
    var locationRequest : LocationRequest? = null
    lateinit var locationCallback: LocationCallback
    val adapter = GroupAdapter <ViewHolder>()
    val userList = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        val backButtonToolbar: ImageView = findViewById(R.id.iv_back_button_new_chat_toolbar)
        rcvUsers = findViewById(R.id.recycler_view_users)
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

        backButtonToolbar.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
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
                                user.distanceFromMe = distancefrommeinkm
                                //Toast.makeText(this,distancefrommeinkm.toString() , Toast.LENGTH_SHORT).show()
                                Log.d("!!!!","$distancefrommeinkm")
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
                                                                //adapter.add(UserItem(user))
                                                                adapter.clear()
                                                                userList.add(user)
                                                                userList.sortByDescending { it.distanceFromMe }
                                                                for (sortedUser in userList) {
                                                                    adapter.add(0, UserItem(sortedUser))
                                                                }
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
    private fun distance(latOne: Double, longOne: Double, latTwo: Double, longTwo: Double): Int {
        val startLocation = Location("")
        startLocation.latitude = latOne
        startLocation.longitude = longOne

        val endLocation = Location("")
        endLocation.latitude = latTwo
        endLocation.longitude = longTwo
        val distanceInKm = startLocation.distanceTo(endLocation) / 1000

        val distance = distanceInKm.roundToInt()
        return distance
    }
}