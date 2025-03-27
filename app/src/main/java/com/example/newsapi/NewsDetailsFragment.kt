package com.example.newsapi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide

class NewsDetailsFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val titleTextView = view.findViewById<TextView>(R.id.textViewDetailedTitle)
        val descTextView = view.findViewById<TextView>(R.id.textViewDetailesDesc)
        val dateTextView = view.findViewById<TextView>(R.id.textViewPublishedAt)
        val sourceTextView = view.findViewById<TextView>(R.id.textViewAuthor)
        val imageView = view.findViewById<ImageView>(R.id.imageView2)
        val contentTextView = view.findViewById<TextView>(R.id.textViewContent)
        val urlTextView = view.findViewById<TextView>(R.id.textViewLink)
        val btnReturn = view.findViewById<Button>(R.id.returnDetails)

        val args = arguments?.let { NewsDetailsFragmentArgs.fromBundle(it) } ?: return



        titleTextView.text = args.title
        descTextView.text = args.desc
        dateTextView.text = args.date
        sourceTextView.text = args.source
        contentTextView.text = args.url
        urlTextView.text = args.content


        Glide.with(requireContext())
            .load(args.imageUrl)
            .placeholder(R.drawable.imageloadfailed)
            .error(R.drawable.imageloadfailed)
            .into(imageView)


        btnReturn.setOnClickListener{
            findNavController().navigate(R.id.action_newsDetailsFragment_to_newsRecyclerFragment)
        }
    }

}