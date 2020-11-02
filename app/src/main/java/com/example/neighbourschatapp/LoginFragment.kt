package com.example.neighbourschatapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment: Fragment() {
    @ExperimentalStdlibApi
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_login, container, false)

        val btnLogin: Button = view.findViewById(R.id.login_button_login)
        val tvGoToRegister: TextView = view.findViewById(R.id.go_to_register_textview)

        btnLogin.setOnClickListener {
            performLogin()
        }

        tvGoToRegister.setOnClickListener {

            val transaction: FragmentTransaction = this.fragmentManager!!.beginTransaction()
            val frag: Fragment = RegisterFragment()
            transaction.replace(R.id.main_fragment_layout, frag)
            transaction.commit()
        }

        return view
    }
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
}