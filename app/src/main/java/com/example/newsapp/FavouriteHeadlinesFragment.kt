package com.example.newsapp

import android.app.Dialog
import android.content.SharedPreferences
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
import com.example.newsapp.api.NewsAPIService
import com.example.newsapp.databinding.FragmentFavouriteHeadlinesBinding
import com.example.newsapp.model.NewsResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FavouriteHeadlinesFragment : Fragment() {

    val BASE_URL: String = "https://newsapi.org/"
    val API_Key: String = "9a31420d0db94cbab8274e37c6fdcda5"

    private lateinit var binding: FragmentFavouriteHeadlinesBinding
    private lateinit var countriesAdapter: CountriesAdapter
    private lateinit var categoryAdapter: CategoriesAdapter
    private var countryNames = ArrayList<String>()
    private var categoryNames = ArrayList<String>()
    private var categoryName: String = ""

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Android 11; Mobile; rv:89.0) Gecko/89.0 Firefox/89.0"
                )
                .method(original.method(), original.body())
                .build()
            chain.proceed(request)
        }
        .build()

    private val retrofit = Retrofit.Builder().baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
        .create(NewsAPIService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavouriteHeadlinesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadData()
        // RecyclerView for Countries
        binding.countryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        countriesAdapter = CountriesAdapter(countryNames, object : CountryListener {
            override fun onCountryClickListener(countryName: String) {
                Log.d("Selected Country", countryName) // Log the selected country

                val call = retrofit.getFavCountries(countryName, API_Key)
                call.enqueue(object : Callback<NewsResponse> {
                    override fun onResponse(
                        call: Call<NewsResponse>,
                        response: Response<NewsResponse>
                    ) {
                        Log.d("Country News", "Successfully received data for: $countryName")
                        val action = FavouriteHeadlinesFragmentDirections
                            .actionFavouriteHeadlinesFragmentToNewsRecyclerFragment(
                                countryName,
                                categoryName
                            )
                        findNavController().navigate(action)
                    }

                    override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                        Log.e("Error", "Failed to fetch country data: ${t.message}")
                    }
                })
            }

        })
        binding.countryRecyclerView.adapter = countriesAdapter

        // RecyclerView for Categories
        binding.categoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        categoryAdapter = CategoriesAdapter(categoryNames, object : CategoriesListener {
            override fun onCategoryClickListener(CategoryName: String) {
                categoryName = CategoryName
                Log.d("Category Name", categoryName)
                // Pass the selected category name to the NewsRecyclerFragment
                val action = FavouriteHeadlinesFragmentDirections
                    .actionFavouriteHeadlinesFragmentToNewsRecyclerFragment(
                        countryNames.toString(),
                        categoryName
                    )
                findNavController().navigate(action)
            }
        })
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
                countryNames.add(countryNameInput)
                countriesAdapter.notifyItemInserted(countryNames.size - 1)
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
                categoryNames.add(categoryNameInput)
                categoryAdapter.notifyItemInserted(categoryNames.size - 1)
                saveData()
                dialog.dismiss()
            } else {
                categoryEditTextInDialog.error = "Category name cannot be empty"
            }
        }
        dialog.show()
    }

    // Save data to Firestore
    private fun saveData() {
        val db = FirebaseFirestore.getInstance()
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            val userId = firebaseUser.uid

            // Get a reference to the user's document in Firestore
            val userDocRef = db.collection("users").document(userId)


            val updates = hashMapOf<String, Any>(
                "countries" to FieldValue.arrayUnion(*countryNames.toTypedArray()),
                "categories" to FieldValue.arrayUnion(*categoryNames.toTypedArray())
            )


            userDocRef.update(updates)
                .addOnSuccessListener {
                    Log.d("Firestore", "User favorites successfully updated")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error updating favorites", e)
                }
        }
    }

    private fun loadData() {
        val db = FirebaseFirestore.getInstance()
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            val userId = firebaseUser.uid

            // Retrieve user favorites from Firestore
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userFavorites = document.data


                        val newCountries =
                            (userFavorites?.get("countries") as? List<String>)?.toMutableList()
                                ?: mutableListOf()
                        countryNames.addAll(newCountries)

                        val newCategories =
                            (userFavorites?.get("categories") as? List<String>)?.toMutableList()
                                ?: mutableListOf()
                        categoryNames.addAll(newCategories)


                        // Log to check data retrieval
                        Log.d("Firestore", "Countries: $countryNames")
                        Log.d("Firestore", "Categories: $categoryNames")

                        // Update the adapters with the loaded data
                        countriesAdapter.notifyDataSetChanged()
                        categoryAdapter.notifyDataSetChanged()
                    } else {
                        Log.d("Firestore", "No favorites found for user")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error loading favorites", e)
                }
        } else {
            Log.e("Firestore", "User not authenticated")
        }
    }


}