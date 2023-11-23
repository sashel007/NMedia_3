package ru.netology.nmedia.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.SingleLiveEvent
import ru.netology.nmedia.recyclerview.OnInteractionListener
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import java.io.IOException
import kotlin.concurrent.thread

private val empty = Post(
    id = 0L,
    author = "Евгений",
    content = "",
    published = 0,
    likedByMe = false,
    likes = 0
//    sharings = 0,
//    video = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository = PostRepositoryImpl()

    private val _data = MutableLiveData(FeedModelState())
    val data: LiveData<FeedModelState>
        get() = _data
    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private var interactionListener: OnInteractionListener? = null

    init {
        loadPosts()
    }

    // Функции для установки обработчика взаимодействий и переменная для хранения
    fun setInteractionListener(listener: OnInteractionListener) {
        this.interactionListener = listener
    }

    fun getInteractionListener(): OnInteractionListener? {
        return interactionListener
    }

    fun loadPosts() {
        thread {
            _data.postValue(FeedModelState(loading = true))
            try {
                val posts = repository.get()
                FeedModelState(posts = posts, empty = posts.isEmpty(), loading = false)
            } catch (e: Exception) {
                FeedModelState(error = true)
            }.let { _data.postValue(it) }
        }
    }

    fun like(id: Long) = thread {
        // Получаем текущий пост
        val currentPosts = _data.value?.posts.orEmpty()
        val currentPost = currentPosts.find { it.id == id }

        currentPost?.let { post ->
            // Оптимистичное обновление UI
            val updatedPost = if (post.likedByMe) {
                post.copy(likes = post.likes - 1, likedByMe = false)
            } else {
                post.copy(likes = post.likes + 1, likedByMe = true)
            }

            // Обновляем список постов
            val updatedPosts = currentPosts.map {
                if (it.id == id) updatedPost else it
            }
            _data.postValue(_data.value?.copy(posts = updatedPosts))

            // Асинхронное обновление на сервере
            try {
                if (post.likedByMe) {
                    repository.unlikePost(id)
                } else {
                    repository.likePost(id)
                }
            } catch (e: IOException) {
                // Если запрос не удался, откатываем изменения в UI
                _data.postValue(_data.value?.copy(posts = currentPosts))
            }
        }
    }

    fun share(id: Long) = thread { repository.share(id) }

    fun removeById(id: Long) = thread {
        thread {
            // Оптимистичная модель
            val old = _data.value?.posts.orEmpty()
            _data.postValue(
                _data.value?.copy(posts = _data.value?.posts.orEmpty()
                    .filter { it.id != id }
                )
            )
            try {
                repository.removeById(id)
            } catch (e: IOException) {
                _data.postValue(_data.value?.copy(posts = old))
            }
        }
    }

    private fun resetEditingState() {
        edited.postValue(empty)
    }

    fun addNewPost(content: String) {
        val newPost = empty.copy(content = content.trim(), id = 0L)
        Log.d("SAVED_POST","$newPost")
        thread {
            val savedPost = repository.save(newPost)
            Log.d("SAVED_POST","$savedPost")

            //Обновление списка постов
            val currentPosts = _data.value?.posts.orEmpty()
            _data.postValue(_data.value?.copy(posts = listOf(savedPost) + currentPosts))

            // Оповещение об успешном создании
            _postCreated.postValue(Unit)
            resetEditingState()
        }
    }

    fun updatePost(postId: Long, content: String) {
        thread {
            // Получаем текущий пост асинхронно
            val currentPost = repository.getById(postId)
            // Проверяем, не является ли текущий пост null
            currentPost?.let {
                // Создаем обновленный пост
                val updatedPost = it.copy(content = content.trim())
                // Сохраняем обновленный пост
                val savedPost = repository.save(updatedPost)
                // Обновляем список постов в LiveData
                val updatedPosts = _data.value?.posts?.map { post ->
                    if (post.id == updatedPost.id) savedPost else post
                }.orEmpty()
                _data.postValue(_data.value?.copy(posts = updatedPosts))
                // Оповещаем о завершении оновления
                _postCreated.postValue(Unit)
            }
            //Сбрасываем состояние редактирования
            resetEditingState()
        }
    }
}


//fun changeContent(content: String) {
//    val text = content.trim()
//    if (edited.value?.content == text) {
//        return
//    }
//    edited.value = edited.value?.copy(content = text)
//}

//fun save() {
//    thread {
//        edited.value?.let { edited ->
//            val post = repository.save(edited)
//            val value = data.value
//            val updatedPosts = value?.posts?.map {
//                if (it.id == edited.id) {
//                    post
//                } else {
//                    it
//                }
//            }.orEmpty()
//
//            val result = if (value?.posts == updatedPosts) {
//                listOf(post) + updatedPosts
//            } else {
//                updatedPosts
//            }
//
//            _data.postValue(
//                value?.copy(posts = result)
//            )
//        }
//        _postCreated.postValue(Unit)
//        edited.postValue(empty)
//    }
//}

//fun save() {
//    edited.value?.let {
//        thread {
//            repository.save(it)
//            _postCreated.postValue(Unit)
//        }
//    }
//    edited.value = empty
//}



