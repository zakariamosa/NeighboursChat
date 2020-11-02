package com.example.neighbourschatapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentTransaction

class LoginFragment: Fragment() {
    @ExperimentalStdlibApi
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_login, container, false)

        val tvAlreadyHaveAccount: TextView = view.findViewById(R.id.go_to_register_textview)

        tvAlreadyHaveAccount.setOnClickListener {

            val transaction: FragmentTransaction = this.fragmentManager!!.beginTransaction()
            val frag: Fragment = RegisterFragment()
            transaction.replace(R.id.main_fragment_layout, frag)
            transaction.commit()
        }

        return view
    }
}