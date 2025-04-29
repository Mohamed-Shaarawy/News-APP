package com.example.newsapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.SignInButton


class SigninFragment : Fragment() {


    lateinit var btnGoogle: SignInButton
    private lateinit var googleSignInHelper: GoogleSignInHelper

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
        return inflater.inflate(R.layout.fragment_signin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        btnGoogle = view.findViewById(R.id.sign_in_button_google)

        googleSignInHelper = GoogleSignInHelper(requireContext()) {
            findNavController().navigate(R.id.action_signinFragment_to_newsRecyclerFragment)
        }

        btnGoogle.setOnClickListener {
            googleSignInHelper.signIn()

        }

    }
}