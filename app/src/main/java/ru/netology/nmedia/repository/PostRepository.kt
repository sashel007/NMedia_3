package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun get(): List<Post>
    fun getById(id: Long): Post
    fun likePost(id: Long)
    fun unlikePost(id: Long)
    fun share(id: Long)
    fun removeById(id: Long)
    fun save(post: Post): Post
}