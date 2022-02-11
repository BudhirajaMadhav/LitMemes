package com.androidmadhav.litmemes

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Query
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class MemeListAdapter(private val listener: IMemeListAdapter): RecyclerView.Adapter<MemeViewHolder>() {
    private var callFetchData: Boolean = false
    private val items: ArrayList<MemeJsonResponse> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemeViewHolder {
        val smallViewsXml = LayoutInflater.from(parent.context).inflate(R.layout.meme_view, parent, false)

        val viewHolder = MemeViewHolder(smallViewsXml)
        viewHolder.memeImage.setOnClickListener{

            listener.memeImageClicked(items[viewHolder.adapterPosition])
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: MemeViewHolder, position: Int) {

        holder.saveButton.setImageResource(R.drawable.save_white_outline)


        if(position % 44 == 0 && callFetchData){

            listener.fetchdata()
            callFetchData = false
        }
        if(position != 0 && position % 35 == 0){

            callFetchData = true
        }

        var imageDrawable: Drawable? = null
        val currentItem = items[position]

        var isGif: Boolean = false

        if(currentItem.url.subSequence(currentItem.url.length - 3, currentItem.url.length) == "gif"){
            isGif = true
        }


        var cachePath:  String? = null
        var savedMeme: SavedMeme? = null

        holder.subreddit.text = "r/" + currentItem.subreddit
        holder.author.text = "Posted by u/${currentItem.author}"
        holder.title.text = currentItem.title
        holder.progressBar.visibility = View.VISIBLE

        Glide.with(holder.memeImage.context).asDrawable().load(currentItem.url).listener(object: RequestListener<Drawable>{
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                holder.progressBar.visibility = View.GONE
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

        holder.upvotes.text = currentItem.ups.toString()


        holder.share.setOnClickListener{
            if(imageDrawable == null){

                listener.notYetLoaded()
            }
            else {
                listener.shareImage(imageDrawable!!, isGif)
            }

        }



        var isSaved = false
        var isDownloaded = false

        holder.saveButton.setOnClickListener(){

            if(!isDownloaded && imageDrawable != null) {
                // This savedMeme is not saved in DB, so its ID is 0
                cachePath = listener.savetoDir(imageDrawable!!, isGif)
                savedMeme = SavedMeme(items[position], cachePath!!)
                isDownloaded = true
            }


            if(imageDrawable == null){

                listener.notYetLoaded()

            }

            else if(savedMeme != null) {

                if(!isSaved) {
                    holder.saveButton.setImageResource(R.drawable.saved_white)
                    listener.saveMeme(savedMeme!!)
                }

                else {

                    holder.saveButton.setImageResource(R.drawable.save_white_outline)

//                    This deleteMeme won't do anything, cuz the current savedMeme does not have ID
                    listener.deleteMeme(savedMeme!!.jsonResponse.url)
                }
            }

            isSaved = !isSaved

        }

    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateMemes(memeJsonResponses: ArrayList<MemeJsonResponse>) {
        val prevSize = items.size
        items.addAll(memeJsonResponses)
        notifyItemRangeInserted(prevSize, 50)

//        DiffUtil::class.java
    }


}

class MemeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val subreddit: TextView = itemView.findViewById(R.id.subreddit)
    val author : TextView = itemView.findViewById(R.id.author)
    val title: TextView = itemView.findViewById(R.id.title)
    val memeImage: ImageView = itemView.findViewById(R.id.memeImage)
    val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
    val upvotes: TextView = itemView.findViewById(R.id.upvotes)
    val share: ImageView = itemView.findViewById(R.id.share)
    val heart: ImageView = itemView.findViewById(R.id.heart)
    val saveButton: ImageView = itemView.findViewById(R.id.save_button)

}

interface IMemeListAdapter{

    fun fetchdata()
    fun memeImageClicked(item: MemeJsonResponse)
    fun shareImage(image: Drawable, isGif: Boolean)
    fun notYetLoaded()
    fun saveMeme(savedMeme: SavedMeme)
    fun savetoDir(image: Drawable, isGif: Boolean): String
    fun deleteMeme(url: String)
}


