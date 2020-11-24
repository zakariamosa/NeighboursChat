package com.example.neighbourschatapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_register.*
import java.util.*

class RegisterFragment: Fragment() {

    lateinit var btnSelectPhotoRegister: Button
    lateinit var ivSelectedImage: CircleImageView
    private var selectedPhotoUri: Uri? = null
    private val REQUEST_LOCATION = 1
    lateinit var locationProvider: FusedLocationProviderClient
    var locationRequest : LocationRequest? = null
    lateinit var locationCallback: LocationCallback
    private var currentuserlat:Double=0.0
    private var currentuserlong:Double=0.0
    lateinit var thisactivity:Activity
    private var userAge = ""
    private var userInterest = ""


    @ExperimentalStdlibApi
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_register, container, false)

        //Här sätter jag rätt variabel till rätt knapp, textView etc..
        btnSelectPhotoRegister = view.findViewById(R.id.btn_select_photo_register)
        ivSelectedImage = view.findViewById(R.id.iv_selected_photo)
        val btnRegister: Button = view.findViewById(R.id.register_button_register)
        val tvBackToLogin: TextView = view.findViewById(R.id.back_to_login)
        val agreeToTerms: CheckBox = view.findViewById(R.id.agree_to_terms_checkbox)
        val tvPrivacyPolicy: TextView = view.findViewById(R.id.tv_privacy_policy)

        btnRegister.setOnClickListener {
            if (agreeToTerms.isChecked) {
                performRegistration()
            }
            else {
                Toast.makeText(activity!!.applicationContext, "To finish registration you have to agree to terms and conditions",
                        Toast.LENGTH_SHORT).show()
            }
        }
        //Denna klicklyssnare låter användaren välja en bild från den enhet som appen används på
        btnSelectPhotoRegister.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
            Log.d("Main", "try to select photo")
        }

        //Denna knapp skickar användaren tillbaka till inloggningssidan
        tvBackToLogin.setOnClickListener {
            val transaction: FragmentTransaction = this.fragmentManager!!.beginTransaction()
            val frag: Fragment = LoginFragment()
            transaction.replace(R.id.main_fragment_layout, frag)
            transaction.commit()
        }
        tvPrivacyPolicy.setOnClickListener {
            context?.let { it1 -> openPrivacy("https://www.dropbox.com/s/zahk5dr8qm5vh2y/Near%20Peer%20Privacy%20Policy.txt?dl=0", it1) }
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

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("Register", "photo was selected")

            //uri representerar vart på enheten bilden är sparad
            selectedPhotoUri = data.data
            //Här använder jag två olika alternativ för att hämta skapa bitmap. Det senare alternativet fungerar inte på modeller äldre än version 28
            val bitmap = when {
                Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(
                        activity!!.applicationContext.contentResolver,
                        selectedPhotoUri
                )
                else -> {
                    val source = ImageDecoder.createSource(activity!!.applicationContext.contentResolver, selectedPhotoUri!!)
                    ImageDecoder.decodeBitmap(source)
                }
            }
            //Här skapar jag en bitmap drawable som jag sedan sätter som bild till ivSelectedImage.
            ivSelectedImage.setImageBitmap(bitmap)
            //Här ändrar jag så att btnSelectedPhoto.. ligger bakom ivSelectedImage och då inte längre syns
            btnSelectPhotoRegister.alpha = 0f
        }
    }
    /*
    Denna funktion skapar en ny användare i firebase databas. Funktionen använder onComplete och onFailure lyssnare.
    Om allt är ifyllt korrekt så skickas användaren till nästa funktion annars så visas en toast där användaren
    meddelas vad som inte är ifyllt korrekt.
     */
    private fun performRegistration() {
        val eMail = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        if (eMail.isEmpty() || password.isEmpty()) {
            Toast.makeText(activity!!.applicationContext, "Please enter both e-mail and password", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("Register", "Email is $eMail")
        Log.d("Register", "Password is $password")

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(eMail, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                Log.d("Register", "user succefully created with uid ${it.result?.user?.uid}")
                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Toast.makeText(activity!!.applicationContext, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT).show()
                Log.d("Register", "Failed to create user: ${it.message}")

            }
    }
    //Denna funktion laddar upp användarens valda bild till firebase storage.
    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return

        //UUID skapar ett nytt slumpat id-nummer
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("Register", "Succesfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("Register", "File location: $it")
                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {
                Log.d("Register", "Failed to upload image")
            }
    }
    private fun saveUserToFirebaseDatabase(userImageUrl: String) {
        val userId = FirebaseAuth.getInstance().uid ?: ""
        val db = FirebaseFirestore.getInstance()
        val user = User(userId, username_edittext_register.text.toString(), email_edittext_register.text.toString(), userImageUrl,
            currentuserlat,currentuserlong, userAge, userInterest, "")

        db.collection("users").document(userId).set(user)
            .addOnSuccessListener {
                Log.d("Register", "We finally saved the user to firebase database!!")

                val intent = Intent(this@RegisterFragment.context, ChatActivity::class.java)
                //Denna rad rensar upp i tidigare aktiviteter
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("Register", "Failed to add user")
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
    fun openPrivacy(urls: String, context: Context) {
        val uris = Uri.parse(urls)
        val intents = Intent(Intent.ACTION_VIEW, uris)
        val b = Bundle()
        b.putBoolean("new_window", true)
        intents.putExtras(b)
        context.startActivity(intents)
    }
}