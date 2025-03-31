package com.example.newsapi

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class FavouriteHeadlinesFragment : Fragment() {

    lateinit var bottomNav: BottomNavigationView
    lateinit var countriesAdapter: CountriesAdapter
    lateinit var countryEditText: EditText
    lateinit var btnAddCountry: Button
    lateinit var btnAddCategory: Button
    var countryName = ArrayList<String>()

    val API_Key: String = "9a31420d0db94cbab8274e37c6fdcda5"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // Handle any arguments passed to the fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favourite_headlines, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("FavouriteHeadlinesFragment", "onViewCreated started")  // Log to confirm the method is called

        bottomNav = view.findViewById(R.id.bottomNavigationView)
        Log.d("FavouriteHeadlinesFragment", "Bottom Navigation initialized")  // Check if the bottomNav is initialized

        // Initialize RecyclerView and Adapter
        val recyclerView: RecyclerView = view.findViewById(R.id.countryRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        Log.d("FavouriteHeadlinesFragment", "RecyclerView layout manager set")  // Confirm RecyclerView setup

        countriesAdapter = CountriesAdapter(countryName)
        recyclerView.adapter = countriesAdapter
        Log.d("FavouriteHeadlinesFragment", "Adapter set to RecyclerView")  // Confirm adapter setup

        // Initialize the button and edit text for adding a country
        btnAddCountry = view.findViewById(R.id.addCountryButton)

        // Initialize the dialog for adding a country
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_view) // Make sure you have this dialog layout XML
        Log.d("FavouriteHeadlinesFragment", "Dialog initialized")  // Confirm dialog setup

        val countryEditTextInDialog: EditText = dialog.findViewById(R.id.dialogCountryEditText)
        val btnAdd: Button = dialog.findViewById(R.id.dialogAddButtonCountry)

        btnAddCountry.setOnClickListener {
            Log.d("FavouriteHeadlinesFragment", "Add Country button clicked")  // Confirm button click

            // Show the dialog when the "Add Country" button is clicked
            btnAdd.setOnClickListener {
                Log.d("FavouriteHeadlinesFragment", "Dialog Add button clicked")  // Confirm dialog's add button click

                val countryNameInput = countryEditTextInDialog.text.toString()
                Log.d("FavouriteHeadlinesFragment", "Country name input: $countryNameInput")  // Log the entered country name

                // Check if the country name is not empty
                if (countryNameInput.isNotEmpty()) {
                    // Add country name to the list
                    countryName.add(countryNameInput)
                    Log.d("FavouriteHeadlinesFragment", "Added country: $countryNameInput")  // Log successful addition

                    // Notify the adapter to refresh the RecyclerView
                    countriesAdapter.notifyItemInserted(countryName.size - 1)
                    Log.d("FavouriteHeadlinesFragment", "Adapter notified of new item")  // Confirm adapter update


                    // Dismiss the dialog after adding the country
                    dialog.dismiss()
                    Log.d("FavouriteHeadlinesFragment", "Dialog dismissed")  // Confirm dialog dismissed
                } else {
                    // Show an error message if input is empty
                    countryEditTextInDialog.error = "Country name cannot be empty"
                    Log.d("FavouriteHeadlinesFragment", "Error: Country name cannot be empty")  // Log error message
                }
            }

            // Show the dialog
            dialog.show()
            Log.d("FavouriteHeadlinesFragment", "Dialog displayed")  // Confirm dialog displayed
        }

        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    Log.d("FavouriteHeadlinesFragment", "Navigating to NewsRecyclerFragment")  // Log navigation
                    findNavController().navigate(R.id.action_favouriteHeadlinesFragment_to_newsRecyclerFragment)
                    true
                }
                else -> false
            }
        }

        Log.d("FavouriteHeadlinesFragment", "onViewCreated completed")  // Confirm method completion
    }

}
