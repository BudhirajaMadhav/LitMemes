package com.androidmadhav.litmemes

data class MemeJsonResponse(
    val postLink: String,
    val subreddit: String,
    val title: String,
    val url: String,
    val nsfw: Boolean,
    val author: String,
    val ups: Int
) {
}