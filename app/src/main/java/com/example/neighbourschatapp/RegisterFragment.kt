package com.example.neighbourschatapp

import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_register.*
import java.util.*

class RegisterFragment: Fragment() {

    lateinit var btnSelectPhotoRegister: Button
    lateinit var ivSelectedImage: CircleImageView
    private var selectedPhotoUri: Uri? = null

    @ExperimentalStdlibApi
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_register, container, false)

        btnSelectPhotoRegister = view.findViewById(R.id.btn_select_photo_register)
        ivSelectedImage = view.findViewById(R.id.iv_selected_photo)
        val btnRegister: Button = view.findViewById(R.id.register_button_register)
        val tvBackToLogin: TextView = view.findViewById(R.id.back_to_login)

        btnRegister.setOnClickListener {
            performRegistration()
        }

        btnSelectPhotoRegister.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
            Log.d("Main", "try to select photo")
        }

        tvBackToLogin.setOnClickListener {
            val transaction: FragmentTransaction = this.fragmentManager!!.beginTransaction()
            val frag: Fragment = LoginFragment()
            transaction.replace(R.id.main_fragment_layout, frag)
            transaction.commit()
        }

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
            //Här skapar jag en bitmap drawable som jag sedan sätter som bild till btnSelectePhoto...
            ivSelectedImage.setImageBitmap(bitmap)
            btnSelectPhotoRegister.alpha = 0f
        }
    }
    private fun performRegistration() {
        val eMail = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        if (eMail.isEmpty() || password.isEmpty()) {
            Toast.makeText(activity!!.applicationContext, "Please enter both e-mail and password", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("Register","Email is $eMail")
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
        val ref = FirebaseDatabase.getInstance().getReference("/users/$userId")

        val user = User(userId, username_edittext_register.text.toString(), email_edittext_register.text.toString(), userImageUrl)

        ref.setValue(user)
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
}