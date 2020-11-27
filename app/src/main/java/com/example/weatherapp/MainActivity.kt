package com.example.weatherapp

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<EditText>(R.id.editTextCityName).doOnTextChanged { text, _, _, _ ->
            Toast.makeText(this, text.toString(), Toast.LENGTH_SHORT).show()
        }

        getRetrofit().create<API>().getPosts().enqueue(object : Callback<List<Post>> {
            override fun onFailure(call: Call<List<Post>>, t: Throwable) {

            }

            override fun onResponse(
                call: Call<List<SmaSdelaesh>>,
                response: Response<List<Post>>
            ) {
                response.body()
            }

        })
    }
}
//view bindind
//toast
//snackbar

fun getRetrofit(): Retrofit {
    val gson = GsonBuilder()
        .setLenient()
        .create()
    val interceptor = HttpLoggingInterceptor()
    interceptor.level = HttpLoggingInterceptor.Level.BODY
    val client = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()

    return Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(client)
        .build()
}

interface API {

    @GET("/posts")
    fun getPosts(): Call<List<Post>>

}

data class SmaSdelaesh(
    val userId: Int
)