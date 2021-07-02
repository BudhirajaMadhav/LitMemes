package com.androidmadhav.litmemes

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class SavedMemesAdapter(private val listener: IMemeListAdapter): RecyclerView.Adapter<MemeViewHolder>() {

    val allMemes: ArrayList<SavedMeme> = ArrayList<SavedMeme>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemeViewHolder {
        val viewHolder = MemeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.meme_view, parent, false))
        viewHolder.saveButton.setImageResource(R.drawable.saved_white)
        return viewHolder
    }

    override fun onBindViewHolder(holder: MemeViewHolder, position: Int) {

        var imageDrawable: Drawable? = null
        val currentItem = allMemes[position]

        holder.subreddit.text = "r/" + currentItem.jsonResponse.subreddit
        holder.author.text = "Posted by u/${currentItem.jsonResponse.author}"
        holder.title.text = currentItem.jsonResponse.title
        holder.progressBar.visibility = View.VISIBLE

        Glide.with(holder.memeImage.context).asDrawable().load(currentItem.imageCachePath).listener(object : RequestListener<Drawable>{
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                if (resource != null) {

                    imageDrawable = resource

                }
                holder.progressBar.visibility = View.GONE
                return false
            }

        }).into(holder.memeImage)

        holder.upvotes.text = currentItem.jsonResponse.ups.toString()

        var isGif: Boolean = false

        if(currentItem.jsonResponse.url.subSequence(currentItem.jsonResponse.url.length - 3, currentItem.jsonResponse.url.length) == "gif"){
            isGif = true
        }

        holder.share.setOnClickListener{
            if(imageDrawable == null){

                listener.notYetLoaded()
            }
            else {
                listener.shareImage(imageDrawable!!, isGif)
            }

        }

        holder.saveButton.setOnClickListener(){

            val savedMeme: SavedMeme = allMemes[position]

            listener.deleteMeme(savedMeme!!.jsonResponse.url)

        }


    }

    override fun getItemCount(): Int {
       return allMemes.size
    }

    fun updateList(newList : List<SavedMeme>) {

        allMemes.clear()
        allMemes.addAll(newList)
        notifyDataSetChanged()

    }
}

//Lets check already existed viewholder kaam krta hai ya nahi
class SavedMemesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)