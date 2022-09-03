package com.avilin.dallemini.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GestureDetectorCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.avilin.dallemini.MainActivity
import com.avilin.dallemini.R
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.lang.Float.max
import java.lang.Float.min
import kotlin.math.abs


class ImageFragment(private val images: List<String>,
                    private val position: Int,
                    private val download: MutableList<Boolean>) : Fragment() {

    private lateinit var btLoad: FloatingActionButton
    private lateinit var btCancel: FloatingActionButton
    private lateinit var ivImage: ImageView

    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private lateinit var imageGestureDetector: GestureDetectorCompat

    private class ScaleListener(private val ivImage: ImageView): ScaleGestureDetector.SimpleOnScaleGestureListener(){
        private var mScaleFactor = 1f

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mScaleFactor *= detector.scaleFactor
            mScaleFactor = max(1f, min(mScaleFactor, 10f))
            ivImage.scaleX = mScaleFactor
            ivImage.scaleY = mScaleFactor
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
            mScaleFactor = ivImage.scaleX
            if (mScaleFactor == 1f){
                ivImage.pivotX = detector!!.focusX - ivImage.left
                ivImage.pivotY = detector.focusY - ivImage.top
            }
            return true
        }
    }

    private class ImageListener(private val ivImage: ImageView,
                                private val images: List<String>,
                                private val download: MutableList<Boolean>,
                                private val btLoad: FloatingActionButton,
                                public var position: Int) : GestureDetector.SimpleOnGestureListener() {

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?,
                             velocityX: Float, velocityY: Float): Boolean {
            if (ivImage.scaleX == 1f && abs(e1!!.y - e2!!.y) < 200){
                if (e1.x - e2.x > 100){
                    if (position + 1 < images.size)
                        position += 1
                    else
                        position = 0

                    Glide.with(ivImage.context).load(images[position]).into(ivImage)
                }

                if (e1.x - e2.x < -100){
                    if (position - 1 > -1)
                        position -= 1
                    else
                        position = images.size - 1

                    Glide.with(ivImage.context).load(images[position]).into(ivImage)
                }

                if(download[position]){
                    btLoad.imageTintList = ColorStateList.valueOf(Color.BLUE)
                    btLoad.isEnabled = false
                }
                else {
                    btLoad.imageTintList = ColorStateList.valueOf(Color.WHITE)
                    btLoad.isEnabled = true
                }
            }
            return true
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            if (ivImage.scaleX == 1f){
                ivImage.scaleX = 2f
                ivImage.scaleY = 2f
                ivImage.pivotX = e!!.x - ivImage.left
                ivImage.pivotY = e.y - ivImage.top
            }
            else{
                ivImage.scaleX = 1f
                ivImage.scaleY = 1f
            }
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?,
                              distanceX: Float, distanceY: Float): Boolean {
            val speed = 1.5f
            val pts = floatArrayOf(0f, 0f)
            ivImage.imageMatrix.mapPoints(pts)
            val scale = ivImage.scaleX

            if (scale != 1f){
                if (ivImage.pivotX + distanceX >= pts[0]
                    && ivImage.pivotX + distanceX <= ivImage.width - pts[0])
                    ivImage.pivotX += distanceX / scale * speed

                if (ivImage.pivotY + distanceY >= pts[1]
                    && ivImage.pivotY + distanceY <= ivImage.height - pts[1])
                    ivImage.pivotY += distanceY / scale * speed
            }

            return true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View =  inflater.inflate(R.layout.fragment_image, container, false)

        btCancel = view.findViewById(R.id.btCancelImage)
        btLoad = view.findViewById(R.id.btLoadImage)
        ivImage = view.findViewById(R.id.ivImage)

        Glide.with(this)
            .load(images[position])
            .error(R.drawable.ic_baseline_do_not_disturb_24)
            .into(ivImage)
        val scaleListener = ScaleListener(ivImage)
        val imageListener = ImageListener(ivImage, images, download, btLoad, position)
        scaleGestureDetector = ScaleGestureDetector(requireContext(), scaleListener)
        imageGestureDetector = GestureDetectorCompat(requireContext(), imageListener)
        view.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                scaleGestureDetector.onTouchEvent(p1)
                imageGestureDetector.onTouchEvent(p1!!)
                return true
            }
        })

        btCancel.setOnClickListener {
            requireActivity().findViewById<ConstraintLayout>(R.id.clContent).visibility = View.VISIBLE
            parentFragmentManager.popBackStack()
        }
        btLoad.setOnClickListener {
            btLoad.imageTintList = ColorStateList.valueOf(Color.BLUE)
            btLoad.isEnabled = false
            download[imageListener.position] = true
            (activity as MainActivity).download(imageListener.position)
        }
        if(download[position]){
            btLoad.imageTintList = ColorStateList.valueOf(Color.BLUE)
            btLoad.isEnabled = false
        }
        return view
    }
}