package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.dao.PostDao
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

    override fun getById(id: Long): LiveData<Post> {
        TODO("Not yet implemented")
    }

    override fun like(id: Long) {
        TODO()
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
            .url("${BASE_URL}/posts")
            .post(gson.toJson(post).toRequestBody(jsonType))
            .build()
        val call = client.newCall(request)
        val response = call.execute()
        val responseString = response.body?.string() ?: error("Body is null")
        return gson.fromJson(responseString, Post::class.java)
    }

}


//    private var posts = emptyList<Post>()
//    private var data = MutableLiveData(posts)
//
//    override fun get(): LiveData<List<Post>> = dao.getAll().map { list ->
//        list.map { it.toDto() }
//    }
//
//
//    override fun save(post: Post) = dao.save(PostEntity.fromDto(post))
//
//    override fun like(id: Long) = dao.like(id)
//
//    override fun removeById(id: Long) = dao.removeById(id)
//
//    override fun share(id: Long) = dao.share(id)
//
//    override fun getById(id: Long): LiveData<Post> = dao.getById(id).map { it.toDto() }