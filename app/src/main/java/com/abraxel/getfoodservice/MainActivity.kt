package com.abraxel.getfoodservice

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import okhttp3.*
import org.apache.commons.text.StringEscapeUtils
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val callButton = findViewById<Button>(R.id.callService);
        val recipeId = findViewById<EditText>(R.id.recipe_id).text
        val sortOrder = findViewById<EditText>(R.id.sort_order).text
        val urlShower = findViewById<ImageView>(R.id.urlShower)


        callButton.setOnClickListener {
            val client = OkHttpClient.Builder()
                .connectTimeout(50, TimeUnit.SECONDS)
                .writeTimeout(50, TimeUnit.SECONDS)
                .readTimeout(50, TimeUnit.SECONDS)
                .build()

            val BASE_URL = "http://192.168.97.174:4000/?recipe_id="+recipeId+"&sort_order="+sortOrder

            val request = Request.Builder()
                .url(BASE_URL)
                .build()

            client.newCall(request).enqueue(object : Callback{
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }
                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                        val jsonString = response.body()?.string()
                        try {
                            val json = JSONObject(jsonString)
                            val cleanedData =
                                removeQuotesAndUnescape(json.getString("data"))?.split(",")?.get(1)?.replace("]", "")?.replace("\"", "")
                            var uiHandler = Handler(Looper.getMainLooper())
                            uiHandler.post(Runnable {
                                Picasso.get().load(cleanedData).into(urlShower)
                            })
                        } catch (e: IOException){
                            Log.e("Error", e.localizedMessage);
                        }


                    }
                }
            })


        }
    }
    private fun removeQuotesAndUnescape(uncleanJson: String): String? {
        val noQuotes = uncleanJson.replace("^\"|\"$".toRegex(), "")
        return StringEscapeUtils.unescapeJava(noQuotes)
    }
}