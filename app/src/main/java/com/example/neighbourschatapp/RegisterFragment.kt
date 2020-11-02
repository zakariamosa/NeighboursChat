package com.example.neighbourschatapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentTransaction

class RegisterFragment: Fragment() {
    @ExperimentalStdlibApi
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_register, container, false)

        val tvBackToLogin: TextView = view.findViewById(R.id.back_to_login)

        tvBackToLogin.setOnClickListener {

            val transaction: FragmentTransaction = this.fragmentManager!!.beginTransaction()
            val frag: Fragment = LoginFragment()
            transaction.replace(R.id.main_fragment_layout, frag)
            transaction.commit()
        }

        return view
    }
}