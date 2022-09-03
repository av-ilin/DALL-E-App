package com.avilin.dallemini.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avilin.dallemini.R
import com.avilin.dallemini.adapters.ImagesRecyclerAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

class GalleryFragment : Fragment() {
    private lateinit var btCancel: FloatingActionButton
    private lateinit var rvGallery: RecyclerView
    private var images: MutableList<String> = mutableListOf()

    private fun fillImages(){
        images = mutableListOf()
        val imagesList = requireActivity().filesDir.listFiles()
        for (img in imagesList!!)
            images.add(img.path)
        images.reverse()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_gallery, container, false)

        btCancel = view.findViewById(R.id.btCancelGallery)
        rvGallery = view.findViewById(R.id.rvGallery)

        btCancel.setOnClickListener {
            requireActivity().findViewById<ConstraintLayout>(R.id.clContent).visibility = View.VISIBLE
            parentFragmentManager.popBackStack()
        }
        rvGallery.setHasFixedSize(true)
        rvGallery.layoutManager = GridLayoutManager(requireContext(), 4)
        return view
    }

    override fun onResume() {
        fillImages()
        rvGallery.adapter = ImagesRecyclerAdapter(images.toList(), object : ImagesRecyclerAdapter.ImagesListener{
            override fun onClick(position: Int, context: Context) {
                val fragment = GalleryViewFragment(images.toList(), position)
                parentFragmentManager.beginTransaction().addToBackStack("GalleryView").replace(R.id.fcvFragments, fragment).commit()
            }
        })
        super.onResume()
    }
}