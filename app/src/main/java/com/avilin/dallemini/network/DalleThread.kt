package com.avilin.dallemini.network

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class DalleThread(private val context: Context) : Thread() {
    var url = ""
    var prompt = ""
    override fun run() {
        val url = URL(url)
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")
        connection.doOutput = true

        val json = "{\"prompt\":\"$prompt\"}"
        connection.outputStream.use { os ->
            val input: ByteArray = json.toByteArray(Charsets.UTF_8)
            os.write(input, 0, input.size)
        }

        val response = StringBuilder()
        BufferedReader(InputStreamReader(connection.inputStream, "utf-8")).use { br ->
            var responseLine: String?
            while (br.readLine().also { responseLine = it } != null) {
                response.append(responseLine!!.trim { it <= ' ' })
            }
        }

        val responseJson = JSONObject(response.toString())
        val images = responseJson.getJSONArray("images")

        val imgUrl = mutableListOf<String>()
        for (i in 0 until images.length()) {
            val img = images.getString(i).toString().replace("\n", "")
            imgUrl.add(img)
        }

        val intent = Intent("Request")
        intent.putExtra("Images", imgUrl.joinToString("|"))
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}