package com.example.newsapi

import android.app.Dialog
import android.content.Context
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
import com.example.newsapi.api.NewsAPIService
import com.example.newsapi.databinding.FragmentFavouriteHeadlinesBinding
import com.example.newsapi.model.NewsResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
    private lateinit var sharedPref: SharedPreferences
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

    private fun saveData() {
        sharedPref = requireContext().getSharedPreferences("SavedData", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        val json = Gson()

        val countryJson = json.toJson(countryNames)
        val categoryJson = json.toJson(categoryNames)

        editor.putString("Countries", countryJson)
        editor.putString("Categories", categoryJson)
        editor.apply()
    }

    private fun loadData() {
        sharedPref = requireContext().getSharedPreferences("SavedData", Context.MODE_PRIVATE)

        val json = Gson()

        val countryJson = sharedPref.getString("Countries", null)
        val categoryJson = sharedPref.getString("Categories", null)

        val arr = object : TypeToken<ArrayList<String>>() {}.type

        if (countryJson != null) {
            try {
                countryNames = json.fromJson(countryJson, arr) ?: ArrayList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            countryNames = ArrayList()
        }

        if (categoryJson != null) {
            try {
                categoryNames = json.fromJson(categoryJson, arr) ?: ArrayList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            categoryNames = ArrayList()
        }
    }
}