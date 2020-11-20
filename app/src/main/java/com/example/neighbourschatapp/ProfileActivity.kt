package com.example.neighbourschatapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_profile.*


class ProfileActivity : AppCompatActivity() {

    var currentUser: User? = null
    lateinit var myProfilePic: CircleImageView
    lateinit var myAge: TextView
    private val RequestCode = 100
    private var imageUri: Uri? = null
    private var storageRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        supportActionBar?.title = "Edit profile"

        storageRef = FirebaseStorage.getInstance().reference.child("images/")
        myProfilePic = findViewById<CircleImageView>(R.id.round_picture)
        myAge = findViewById(R.id.age_profile)
        storageRef = FirebaseStorage.getInstance().reference.child("images/")
        fetchCurrentUser()

        save_button_profile.setOnClickListener{
            currentUser!!.userAge = myAge.text.toString()
            currentUser!!.userInterest = interest_profile.text.toString()
            FirebaseFirestore.getInstance().collection("users").document(currentUser!!.userId).set(currentUser!!)
        }
        change_picture_profile.setOnClickListener {
            pickImage()
        }
    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, RequestCode )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RequestCode && resultCode == Activity.RESULT_OK && data!!.data != null) {
            imageUri = data.data
            Log.d("Profile", "Uploading...")
            uploadImageToDatabase()
        }
    }

    private fun uploadImageToDatabase() {
        val progressBar = ProgressDialog(this)
        progressBar.setMessage("Image is being uploaded, please wait!")
        progressBar.show()

        if(imageUri != null) {

            val fileRef = storageRef!!.child(System.currentTimeMillis().toString() + ".jpg")
            val uploadTask: StorageTask<*>
            uploadTask = fileRef.putFile(imageUri!!)

            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->

                if(!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener{ task ->
                if(task.isSuccessful) {
                    val downloadUrl = task.result
                    var mUri = downloadUrl.toString()
                    currentUser!!.userImageUrl = mUri
                    FirebaseFirestore.getInstance().collection("users").document(currentUser!!.userId).set(currentUser!!)
                    var userPic = currentUser?.userImageUrl
                    Picasso.get().load(userPic).into(myProfilePic)
                    progressBar.dismiss()
                }
            }

        }
    }

    private fun fetchCurrentUser() {

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val db = FirebaseFirestore.getInstance().collection("users").document(uid)
        db.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null) {
                    currentUser = documentSnapshot.toObject(User::class.java)
                    Log.d("Profile", "CurrentUsername from FetchUsers: $currentUser")
                    name_profile.setText(currentUser!!.userName)
                    interest_profile.setText(currentUser!!.userInterest)
                    age_profile.setText(currentUser!!.userAge)
                    var userPic = currentUser?.userImageUrl
                    Picasso.get().load(userPic).into(myProfilePic)
                    profile_button.alpha = 0f
                    Log.d("Profile", "UserImageUrl: $userPic")
                }
            }
    }
}