package com.example.newsapi

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapi.databinding.FragmentFavouriteHeadlinesBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FavouriteHeadlinesFragment : Fragment() {

    private lateinit var binding: FragmentFavouriteHeadlinesBinding
    private lateinit var sharedPref: SharedPreferences
    private lateinit var countriesAdapter: CountriesAdapter
    private lateinit var categoryAdapter: CategoriesAdapter
    private var countryName = ArrayList<String>()
    private var categoryName = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout using View Binding
        binding = FragmentFavouriteHeadlinesBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadData()

        // RecyclerView for Countries
        binding.countryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        countriesAdapter = CountriesAdapter(countryName)
        binding.countryRecyclerView.adapter = countriesAdapter

        // RecyclerView for Categories
        binding.categoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        categoryAdapter = CategoriesAdapter(categoryName)
        binding.categoryRecyclerView.adapter = categoryAdapter


        binding.addCountryButton.setOnClickListener {
            addCountryDialog()
        }


        binding.addCategoryButton.setOnClickListener {
            addCategoryDialog()
        }

        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    findNavController().navigate(R.id.action_favouriteHeadlinesFragment_to_newsRecyclerFragment)
                    true
                }

                else -> false
            }
        }
    }

    private fun addCountryDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_view)
        val countryEditTextInDialog: EditText = dialog.findViewById(R.id.dialogCountryEditText)
        val btnAdd: Button = dialog.findViewById(R.id.dialogAddButtonCountry)

        btnAdd.setOnClickListener {
            val countryNameInput = countryEditTextInDialog.text.toString()
            if (countryNameInput.isNotEmpty()) {
                countryName.add(countryNameInput)
                countriesAdapter.notifyItemInserted(countryName.size - 1)
                saveData()
                dialog.dismiss()
            } else {
                countryEditTextInDialog.error = "Country name cannot be empty"
            }
        }
        dialog.show()
    }

    private fun addCategoryDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.category_dialog_view)
        val categoryEditTextInDialog: EditText = dialog.findViewById(R.id.dialogCategoryEditText)
        val btnAdd: Button = dialog.findViewById(R.id.dialogAddButtonCategory)

        btnAdd.setOnClickListener {
            val categoryNameInput = categoryEditTextInDialog.text.toString()
            if (categoryNameInput.isNotEmpty()) {
                categoryName.add(categoryNameInput)
                categoryAdapter.notifyItemInserted(categoryName.size - 1)
                saveData()
                dialog.dismiss()
            } else {
                categoryEditTextInDialog.error = "Category name cannot be empty"
            }
        }
        dialog.show()
    }

    private fun saveData() {
        sharedPref = requireContext().getSharedPreferences("SavedData", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        val json = Gson()

        val countryJson = json.toJson(countryName)
        val categoryJson = json.toJson(categoryName)

        editor.putString("Countries", countryJson)
        editor.putString("Categories", categoryJson)
        editor.apply()
    }

    private fun loadData() {
        sharedPref = requireContext().getSharedPreferences("SavedData", Context.MODE_PRIVATE)

        val json = Gson()

        var countryJson = sharedPref.getString("Countries", null)
        var categoryJson = sharedPref.getString("Categories", null)

        val arr = object : TypeToken<ArrayList<String>>() {}.type

        if (countryJson != null) {
            try {
                countryName = json.fromJson(countryJson, arr) ?: ArrayList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            countryName = ArrayList()
        }

        // Check if categoryJson is not null before converting
        if (categoryJson != null) {
            try {
                categoryName = json.fromJson(categoryJson, arr) ?: ArrayList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            categoryName = ArrayList()
        }
    }
}
