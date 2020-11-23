package com.example.neighbourschatapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

       Handler().postDelayed({
           if (onboardingFinished()){
               val intent = Intent(requireActivity(), MainActivity::class.java)
               startActivity(intent)
           } else {
               findNavController().navigate(R.id.action_splashFragment_to_viewPagerFragment)
           }
       }, 3000)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    private fun onboardingFinished():Boolean{

        val sharedPreferences= requireActivity().getSharedPreferences("onboarding", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("Finished", false)
    }

}