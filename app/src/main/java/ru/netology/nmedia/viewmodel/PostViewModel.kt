package ru.netology.nmedia.viewmodel

import android.app.Application
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
    content = "",
    author = "",
    likedByMe = false,
    likes = 0,
    published = "",
    sharings = 0,
    video = ""
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

    fun getPostById(id: Long) = thread { repository.getById(id) }

    fun like(id: Long) = thread { repository.like(id) }

    fun share(id: Long) = thread { repository.share(id) }

    fun removeById(id: Long) = thread {
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

    private fun resetEditingState() {
        edited.postValue(empty)
    }

    fun addNewPost(content: String) {
        val newPost = empty.copy(content = content.trim(), id = 0L)
        thread {
            val savedPost = repository.save(newPost)

            //Обновление списка постов
            val currentPosts = _data.value?.posts.orEmpty()
            _data.postValue(_data.value?.copy(posts = listOf(savedPost) + currentPosts))

            // Оповещение об успешном создании
            _postCreated.postValue(Unit)
        }
        resetEditingState()
    }

    fun updatePost(postId: Long, content: String) {
        thread {
            // Получаем текущий пост асинхронно
            val currentPost = repository.getById(postId).value
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