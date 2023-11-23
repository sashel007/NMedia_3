package ru.netology.nmedia.dto

data class Post(
    var id: Long,
    val author: String = "Евгений",
    val content: String,
    val published: String = "Сейчас",
    val likedByMe: Boolean = false,
    val likes: Int,
    var sharings: Int,
    var video: String? = ""
) {
}
