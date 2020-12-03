package com.example.weatherapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.dataClasses.Post
import com.example.weatherapp.databinding.ActivityListBinding
import java.io.Serializable

class ListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val posts = (intent.getSerializableExtra("data") as Kostil).lists
        binding.list.adapter = PostsAdapter(posts)
        binding.list.layoutManager = LinearLayoutManager(this)
    }
}

class Kostil(
    val lists: List<Post>
) : Serializable