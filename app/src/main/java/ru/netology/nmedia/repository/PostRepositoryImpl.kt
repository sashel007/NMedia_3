package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit

class PostRepositoryImpl() : PostRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val postsType = object : TypeToken<List<Post>>() {}.type

    private companion object {
        const val GET = ""
        const val BASE_URL = "http://10.0.2.2:9999"
        val jsonType = "application/json".toMediaType()
    }

    override fun get(): List<Post> {
        val request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts")
            .build()
        val call = client.newCall(request)
        val response = call.execute()
        val responseString = response.body?.string() ?: error("Body is null")
        return gson.fromJson(responseString, postsType)
    }

    override fun getById(id: Long): Post {
        val request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()
        val call = client.newCall(request)
        val response = call.execute()
        val responseString = response.body?.string() ?: error("Body is null")
        return gson.fromJson(responseString, Post::class.java)
    }

    override fun likePost(id: Long) {
        val request = Request.Builder()
            .post(RequestBody.create(null, ByteArray(0)))
            .url("${BASE_URL}/api/slow/posts/$id/likes")
            .build()
        client.newCall(request)
            .execute()
            .close()
    }

    override fun unlikePost(id: Long) {
        val request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/posts/$id/likes")
            .build()
        client.newCall(request)
            .execute()
            .close()
    }

    override fun share(id: Long) {
        TODO("Not yet implemented")
    }

    override fun removeById(id: Long) {
        val request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()
        client.newCall(request)
            .execute()
            .close()
    }

    override fun save(post: Post): Post {
        val request = Request.Builder()
            .url("${BASE_URL}/api/posts")
            .post(gson.toJson(post).toRequestBody(jsonType))
            .build()
        val call = client.newCall(request)
        val response = call.execute()
        val responseString = response.body?.string() ?: error("Body is null")
        return gson.fromJson(responseString, Post::class.java)
    }
}