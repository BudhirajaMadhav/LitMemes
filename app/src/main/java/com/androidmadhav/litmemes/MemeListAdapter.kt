package com.androidmadhav.litmemes

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class MemeListAdapter(private val listener: AddOns): RecyclerView.Adapter<MemeViewHolder>() {
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

        if(position % 44 == 0 && callFetchData){

            listener.fetchdata()
            callFetchData = false
        }
        if(position != 0 && position % 35 == 0){

            callFetchData = true
        }

        lateinit var imageDrawable: Drawable
        val currentItem = items[position]

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

        holder.heart.setImageResource(R.drawable.like)
        holder.upvotes.text = currentItem.ups.toString()
        holder.share.setImageResource(R.drawable.share)

        holder.share.setOnClickListener{
            if(currentItem.url.subSequence(currentItem.url.length - 3, currentItem.url.length) == "gif"){

                listener.shareGif(imageDrawable)

            }else {
                listener.shareImg(imageDrawable)
            }

        }


    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateMemes(memeJsonResponses: ArrayList<MemeJsonResponse>) {
        items.addAll(memeJsonResponses)
        notifyDataSetChanged()

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

}

interface AddOns{

    fun fetchdata()
    fun memeImageClicked(item: MemeJsonResponse)
    fun shareImg(image: Drawable)
    fun shareGif(image: Drawable)
}
