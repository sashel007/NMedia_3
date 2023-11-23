package ru.netology.nmedia.recyclerview

import android.view.View
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.functions.formatAmount
import ru.netology.nmedia.databinding.PostCardBinding
import ru.netology.nmedia.dto.Post

class PostViewHolder(
    private val binding: PostCardBinding, private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {


    fun bind(post: Post) {
        val videoCLickListener = View.OnClickListener {
            onInteractionListener.playVideo(post.video)
        }
        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            likeIcon.isChecked = post.likedByMe
            likeIcon.text = formatAmount(post.likes)
            likeIcon.setOnClickListener {
                onInteractionListener.like(post)
            }
            sharingIcon.setOnClickListener {
                onInteractionListener.share(post)
            }
            sharingIcon.text = formatAmount(post.sharings)
            if (post.video.isNullOrEmpty()) {
                videoGroupViews.visibility = View.GONE
            } else {
                videoGroupViews.visibility = View.VISIBLE
                playButton.setOnClickListener(videoCLickListener)
                videoImage.setOnClickListener(videoCLickListener)
            }
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.menu_options)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.remove(post)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.edit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
            clickableView.setOnClickListener {
                onInteractionListener.onPostClicked(post)
            }

        }
    }
}