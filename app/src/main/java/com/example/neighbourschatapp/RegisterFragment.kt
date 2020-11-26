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
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
                Toast.makeText(requireActivity().applicationContext, "To finish registration you have to agree to terms and conditions",
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
            val transaction: FragmentTransaction = this.requireFragmentManager().beginTransaction()
            val frag: Fragment = LoginFragment()
            transaction.replace(R.id.main_fragment_layout, frag)
            transaction.commit()
        }
        tvPrivacyPolicy.setOnClickListener {
            val builder = this@RegisterFragment.context?.let { it1 -> AlertDialog.Builder(it1) }
            builder!!.setMessage("**Privacy Policy**\n" +
                    "\n" +
                    "Near Peer Team built the Near Peer app as a Free app. This SERVICE is provided by Near Peer Team at no cost and is intended for use as is.\n" +
                    "\n" +
                    "This page is used to inform visitors regarding my policies with the collection, use, and disclosure of Personal Information if anyone decided to use my Service.\n" +
                    "\n" +
                    "If you choose to use my Service, then you agree to the collection and use of information in relation to this policy. The Personal Information that I collect is used for providing and improving the Service. I will not use or share your information with anyone except as described in this Privacy Policy.\n" +
                    "\n" +
                    "The terms used in this Privacy Policy have the same meanings as in our Terms and Conditions, which is accessible at Near Peer unless otherwise defined in this Privacy Policy.\n" +
                    "\n" +
                    "**Information Collection and Use**\n" +
                    "\n" +
                    "For a better experience, while using our Service, I may require you to provide us with certain personally identifiable information, including but not limited to Email address, user ID, location, profile picture. The information that I request will be retained on your device and is not collected by me in any way.\n" +
                    "\n" +
                    "The app does use third party services that may collect information used to identify you.\n" +
                    "\n" +
                    "Link to privacy policy of third party service providers used by the app\n" +
                    "\n" +
                    "*   [Google Play Services](https://www.google.com/policies/privacy/)\n" +
                    "\n" +
                    "**Log Data**\n" +
                    "\n" +
                    "I want to inform you that whenever you use my Service, in a case of an error in the app I collect data and information (through third party products) on your phone called Log Data. This Log Data may include information such as your device Internet Protocol (“IP”) address, device name, operating system version, the configuration of the app when utilizing my Service, the time and date of your use of the Service, and other statistics.\n" +
                    "\n" +
                    "**Cookies**\n" +
                    "\n" +
                    "Cookies are files with a small amount of data that are commonly used as anonymous unique identifiers. These are sent to your browser from the websites that you visit and are stored on your device's internal memory.\n" +
                    "\n" +
                    "This Service does not use these “cookies” explicitly. However, the app may use third party code and libraries that use “cookies” to collect information and improve their services. You have the option to either accept or refuse these cookies and know when a cookie is being sent to your device. If you choose to refuse our cookies, you may not be able to use some portions of this Service.\n" +
                    "\n" +
                    "**Service Providers**\n" +
                    "\n" +
                    "I may employ third-party companies and individuals due to the following reasons:\n" +
                    "\n" +
                    "*   To facilitate our Service;\n" +
                    "*   To provide the Service on our behalf;\n" +
                    "*   To perform Service-related services; or\n" +
                    "*   To assist us in analyzing how our Service is used.\n" +
                    "\n" +
                    "I want to inform users of this Service that these third parties have access to your Personal Information. The reason is to perform the tasks assigned to them on our behalf. However, they are obligated not to disclose or use the information for any other purpose.\n" +
                    "\n" +
                    "**Security**\n" +
                    "\n" +
                    "I value your trust in providing us your Personal Information, thus we are striving to use commercially acceptable means of protecting it. But remember that no method of transmission over the internet, or method of electronic storage is 100% secure and reliable, and I cannot guarantee its absolute security.\n" +
                    "\n" +
                    "**Links to Other Sites**\n" +
                    "\n" +
                    "This Service may contain links to other sites. If you click on a third-party link, you will be directed to that site. Note that these external sites are not operated by me. Therefore, I strongly advise you to review the Privacy Policy of these websites. I have no control over and assume no responsibility for the content, privacy policies, or practices of any third-party sites or services.\n" +
                    "\n" +
                    "**Children’s Privacy**\n" +
                    "\n" +
                    "These Services do not address anyone under the age of 13. I do not knowingly collect personally identifiable information from children under 13\\. In the case I discover that a child under 13 has provided me with personal information, I immediately delete this from our servers. If you are a parent or guardian and you are aware that your child has provided us with personal information, please contact me so that I will be able to do necessary actions.\n" +
                    "\n" +
                    "**Changes to This Privacy Policy**\n" +
                    "\n" +
                    "I may update our Privacy Policy from time to time. Thus, you are advised to review this page periodically for any changes. I will notify you of any changes by posting the new Privacy Policy on this page.\n" +
                    "\n" +
                    "This policy is effective as of 2020-11-24\n" +
                    "\n" +
                    "**Contact Us**\n" +
                    "\n" +
                    "If you have any questions or suggestions about my Privacy Policy, do not hesitate to contact me at enar.warfvinge@iths.se.\n" +
                    "\n" +
                    "This privacy policy page was created at [privacypolicytemplate.net](https://privacypolicytemplate.net) and modified/generated by [App Privacy Policy Generator](https://app-privacy-policy-generator.nisrulz.com/)")
                .setCancelable(false)
                .setPositiveButton("Ok") { dialog, id ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }
        thisactivity= requireActivity()
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
                        requireActivity().applicationContext.contentResolver,
                        selectedPhotoUri
                )
                else -> {
                    val source = ImageDecoder.createSource(requireActivity().applicationContext.contentResolver, selectedPhotoUri!!)
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
            Toast.makeText(requireActivity().applicationContext, "Please enter both e-mail and password", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireActivity().applicationContext, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT).show()
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
                saveLocationSettings()

                val intent = Intent(this@RegisterFragment.context, ChatActivity::class.java)
                //Denna rad rensar upp i tidigare aktiviteter
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)


            }
            .addOnFailureListener {
                Log.d("Register", "Failed to add user")
            }
    }
    private fun saveLocationSettings() {
        val userId = FirebaseAuth.getInstance().uid ?: ""
        val db = FirebaseFirestore.getInstance()
        val userSettings = UserSettings(5)

        db.collection("Settings").document(userId).set(userSettings)
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