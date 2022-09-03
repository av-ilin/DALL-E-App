package com.avilin.dallemini.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import java.io.*

class LoadThread(private val base64String: String, private val filesDir: File): Thread(){

    private fun generateID(): String{
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        var id =  (1..6).map{ allowedChars.random() }.joinToString("")
        val fileList = filesDir.list()
        while ("$id.jpg" in fileList!!)
            id =  (1..6).map{ allowedChars.random() }.joinToString("")
        return id
    }

    private fun load(): Bitmap?{
        val base64Image = base64String.split(",")[1]
        val decodedString: ByteArray = Base64.decode(base64Image, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }

    private fun save(bitmap: Bitmap?){
        if (bitmap == null) {
            Log.d("LoadThread", "Error!(Save)")
            return
        }
        try{
            val imageName = generateID() + ".jpg"
            val imageFile = File(filesDir, imageName)
            val fos = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()
        } catch (e: FileNotFoundException) {
            Log.d("LoadThread", "File not found!!(Save)")
        } catch (e: IOException) {
            Log.d("LoadThread", "Error accessing file!!(Save)")
        }
    }

    override fun run() {
        save(load())
    }
}

/*
private fun load(string: String): Bitmap? {
    try {
        val url = URL(string)
        val connection = url.openConnection() as HttpURLConnection
        connection.connect()
        val inputStream: InputStream = connection.inputStream
        val bufferedInputStream = BufferedInputStream(inputStream)
        return BitmapFactory.decodeStream(bufferedInputStream)
    } catch (e: IOException) {
        e.printStackTrace()
        Log.d("LoadThread", "Error!(Load)")
    } catch (e: MalformedURLException) {
        e.printStackTrace()
        Log.d("LoadThread", "ParseError!(Load)")
    }
    return null
}
*/