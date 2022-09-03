package com.avilin.dallemini.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import androidx.core.view.GestureDetectorCompat
import com.avilin.dallemini.R
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import kotlin.math.abs

class GalleryViewFragment(
    private val images: List<String>,
    private val position: Int
) : Fragment() {
    private val delete = MutableList<Boolean>(images.size){false}

    private lateinit var ivGallery: ImageView
    private lateinit var btCancel: FloatingActionButton
    private lateinit var btDelete: FloatingActionButton

    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private lateinit var imageGestureDetector: GestureDetectorCompat

    private class ScaleListener(private val ivImage: ImageView): ScaleGestureDetector.SimpleOnScaleGestureListener(){
        private var mScaleFactor = 1f

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mScaleFactor *= detector.scaleFactor
            mScaleFactor = java.lang.Float.max(1f, java.lang.Float.min(mScaleFactor, 10f))
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
                                private val delete: MutableList<Boolean>,
                                private val btDelete: FloatingActionButton,
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

                if(delete[position])
                    btDelete.imageTintList = ColorStateList.valueOf(Color.RED)
                else
                    btDelete.imageTintList = ColorStateList.valueOf(Color.WHITE)
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

    private fun deleteImg(){
        for (i in images.indices)
            if (delete[i])
                File(images[i]).delete()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_gallery_view, container,false)

        ivGallery = view.findViewById(R.id.ivGallery)
        btCancel = view.findViewById(R.id.btCancelView)
        btDelete = view.findViewById(R.id.btDeleteImage)

        Glide.with(this)
            .load(images[position])
            .error(R.drawable.ic_baseline_do_not_disturb_24)
            .into(ivGallery)

        val scaleListener = ScaleListener(ivGallery)
        val imageListener = ImageListener(ivGallery, images, delete, btDelete, position)
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
            parentFragmentManager.popBackStack()
        }
        btDelete.setOnClickListener {
            val pos = imageListener.position
            delete[pos] = !delete[pos]
            if(delete[pos])
                btDelete.imageTintList = ColorStateList.valueOf(Color.RED)
            else
                btDelete.imageTintList = ColorStateList.valueOf(Color.WHITE)
        }

        return view
    }

    override fun onDestroy() {
        deleteImg()
        super.onDestroy()
    }
}