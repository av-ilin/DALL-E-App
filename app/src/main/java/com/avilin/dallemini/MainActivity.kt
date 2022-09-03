package com.avilin.dallemini

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avilin.dallemini.adapters.ImagesRecyclerAdapter
import com.avilin.dallemini.fragments.GalleryFragment
import com.avilin.dallemini.network.DalleThread
import com.avilin.dallemini.fragments.ImageFragment
import com.avilin.dallemini.network.LoadThread
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


class MainActivity : AppCompatActivity() {
    private var serviceWork = false
    private var activityWork = true
    private val url = "https://backend.craiyon.com/generate"
    private val img = "data:image/jpeg;base64,"
    private var images = mutableListOf<String>()
    private var downloadedImg = mutableListOf<Boolean>()
    private val CHANNEL_ID = "SuccessResponse"

    private lateinit var pbLoad: ProgressBar
    private lateinit var btStart: FloatingActionButton
    private lateinit var etPrompt: EditText
    private lateinit var rvImages: RecyclerView
    private lateinit var btGallery: FloatingActionButton
    private lateinit var clContent: ConstraintLayout
    private lateinit var btExit: FloatingActionButton

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            images = p1!!.getStringExtra("Images")!!.split("|").toMutableList()
            downloadedImg = MutableList(images.size){false}
            for (i in 0 until images.size)
                images[i] = img + images[i]

            rvImages.adapter = ImagesRecyclerAdapter(images.toList(), object : ImagesRecyclerAdapter.ImagesListener{
                override fun onClick(position: Int, context: Context) {
                    clContent.visibility = View.GONE
                    val fragment = ImageFragment(images.toList(), position, downloadedImg)
                    supportFragmentManager.beginTransaction().addToBackStack("Image").add(R.id.fcvFragments, fragment).commit()
                }
            })

            pbLoad.visibility = View.GONE
            serviceWork = false

            if (!activityWork){
                val builder = NotificationCompat.Builder(this@MainActivity, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_baseline_photo_24)
                    .setContentTitle(resources.getString(R.string.notification_title))
                    .setContentText(resources.getString(R.string.notification_content))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                with(NotificationManagerCompat.from(this@MainActivity)) {
                    // notificationId is a unique int for each notification that you must define
                    notify(13, builder.build())
                }
            }
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    public fun download(position: Int){
        LoadThread(images[position], filesDir).start()
        downloadedImg[position] = true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (clContent.visibility == View.GONE && supportFragmentManager.backStackEntryCount == 0)
            clContent.visibility = View.VISIBLE
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createNotificationChannel()

        pbLoad = findViewById(R.id.pbLoad)
        btStart= findViewById(R.id.btStart)
        etPrompt = findViewById(R.id.etPrompt)
        rvImages = findViewById(R.id.rvImages)
        btGallery = findViewById(R.id.btGallery)
        clContent = findViewById(R.id.clContent)
        btExit = findViewById(R.id.btExit)

        pbLoad.visibility = View.GONE
        btStart.setOnClickListener {
            etPrompt.clearFocus()
            val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(etPrompt.windowToken, 0)
            if (!serviceWork) {
                val dalleThread = DalleThread(this)
                dalleThread.url = url
                dalleThread.prompt = etPrompt.text.toString()
                dalleThread.start()
                serviceWork = true
                pbLoad.visibility = View.VISIBLE
            }
            else
                Toast.makeText(this, "Wait, please", Toast.LENGTH_LONG).show()
        }
        btExit.setOnClickListener { finishAffinity() }
        btGallery.setOnClickListener {
            clContent.visibility = View.GONE
            val fragment = GalleryFragment()
            supportFragmentManager.beginTransaction().addToBackStack("Gallery").add(R.id.fcvFragments, fragment).commit()
        }
        rvImages.setHasFixedSize(true)
        rvImages.layoutManager = GridLayoutManager(this, 2)
        rvImages.adapter = ImagesRecyclerAdapter(images.toList(), object : ImagesRecyclerAdapter.ImagesListener{
            override fun onClick(position: Int, context: Context) {
                clContent.visibility = View.GONE
                val fragment = ImageFragment(images.toList(), position, downloadedImg)
                supportFragmentManager.beginTransaction().addToBackStack("Image").add(R.id.fcvFragments, fragment).commit()
            }
        })
    }

    override fun onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, IntentFilter("Request"))
        activityWork = true
        super.onResume()
    }

    override fun onPause() {
        activityWork = false
        super.onPause()
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }
}

/*
https://www.craiyon.com/
https://github.com/borisdayma/dalle-mini.git
API:
url = "https://backend.craiyon.com/generate"
img = "data:image/jpeg;base64,"
prompt = 'i need a think'
data = {"prompt": prompt}
request = requests.post(url, json=data)
response = request.json()
photo = response['images'][1].replace('\n', '')
result = img + photo
*/