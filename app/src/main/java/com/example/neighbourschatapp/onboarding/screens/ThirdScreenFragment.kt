package com.example.neighbourschatapp.onboarding.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.neighbourschatapp.ChatActivity
import com.example.neighbourschatapp.MainActivity
import com.example.neighbourschatapp.R
import kotlinx.android.synthetic.main.fragment_first_screen.view.*
import kotlinx.android.synthetic.main.fragment_third_screen.view.*

class ThirdScreenFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_third_screen, container, false)

        view.finish.setOnClickListener {
            //findNavController().navigate(R.id.action_viewPagerFragment_to_mainActivity)
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
            onboardingFinished()
        }

        return view
    }


    private fun onboardingFinished(){

        val sharedPreferences= requireActivity().getSharedPreferences("onboarding", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putBoolean("Finished", true)
        editor.apply()



    }


}