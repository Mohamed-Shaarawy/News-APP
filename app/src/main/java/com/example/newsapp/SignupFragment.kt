package com.example.newsapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.navigation.fragment.findNavController


class SignupFragment : Fragment() {

    lateinit var FName:EditText
    lateinit var LName:EditText
    lateinit var username:EditText
    lateinit var  password:EditText
    lateinit var signIn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FName = view.findViewById(R.id.EditTextFirstNameSignup)
        LName = view.findViewById(R.id.EditTextLastNameSignup)
        username = view.findViewById(R.id.EditTextUsernameSignup)
        password = view.findViewById(R.id.EditTextPasswordSignup)
        signIn = view.findViewById(R.id.buttonSignInSignup)

        signIn.setOnClickListener{
        findNavController().navigate(R.id.action_signupFragment_to_signinFragment)
        }


    }
}
