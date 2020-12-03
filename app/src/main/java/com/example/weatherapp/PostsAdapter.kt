package com.example.weatherapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.dataClasses.Post
import com.example.weatherapp.databinding.ItemPostBinding

class PostsAdapter(
    private val posts: List<Post>
) : RecyclerView.Adapter<PostsAdapter.PostVH>() {

    class PostVH(private val view: ItemPostBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(post: Post) {
            view.apply {
                titleTv.text = post.title
                bodyTv.text = post.body
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PostVH(
            ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun getItemCount() = posts.size

    override fun onBindViewHolder(holder: PostVH, position: Int) {
       holder.bind(posts[position])
    }

}