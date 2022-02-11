package com.androidmadhav.litmemes

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer


open class MainActivity : AppCompatActivity(), IMemeListAdapter {

    private var mAdapter = MemeListAdapter(this)
    private lateinit var mLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        Set layout manager
        mLayoutManager = LinearLayoutManager(this)
        MemeRecyclerView.layoutManager = mLayoutManager

//        Fetch 1st set of data
        fetchdata()

//        Set the adapter
        MemeRecyclerView.adapter = mAdapter

//        addScrollListener()

        floatingActionButton.setOnClickListener {
            val intent = Intent(this, SavedMemesActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }
    }

    private fun addScrollListener() {
        MemeRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener()
        {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if(!MemeRecyclerView.canScrollVertically(1)) {
                    fetchdata()
                }

            }
        })
    }

    //    Clear glide cache
private fun clearGlideCache() = Glide.get(this).clearDiskCache()


    //Fires after the OnStop() state
    override fun onDestroy() {
        super.onDestroy()
//        try {
//            trimCache(this)

//            Cleared th--e glide cache when back button is pressed
            clearGlideCache()

//        } catch (e: Exception) {
//            // TODO Auto-generated catch block
//            e.printStackTrace()
//        }
    }

//    private fun trimCache(context: Context) {
//        try {
//            val dir = context.cacheDir
//            if (dir != null && dir.isDirectory) {
//                deleteDir(dir)
//            }
//        } catch (e: Exception) {
//            // TODO: handle exception
//        }
//    }
//
//    private fun deleteDir(dir: File?): Boolean {
//        if (dir != null && dir.isDirectory) {
//            val children = dir.list()
//            for (i in children.indices) {
//                val success = deleteDir(File(dir, children[i]))
//                if (!success) {
//                    return false
//                }
//            }
//        }
//
////         The directory is now empty so delete it
//        return dir!!.delete()
//    }


//    Fetching response from API and updating into adapter
    override fun fetchdata() {
        val url = "https://meme-api.herokuapp.com/gimme/50"

        val memeJsonResponses: ArrayList<MemeJsonResponse> = ArrayList()

            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                {
                    val memeJsonArray = it.getJSONArray("memes")
                    for (idx in 0 until memeJsonArray.length()){
                        val memeJSONObject = memeJsonArray.get(idx) as JSONObject
                        val requiredMemeFormat = MemeJsonResponse(
                            memeJSONObject.getString("postLink"),
                            memeJSONObject.getString("subreddit"),
                            memeJSONObject.getString("title"),
                            memeJSONObject.getString("url"),
                            memeJSONObject.getBoolean("nsfw"),
                            memeJSONObject.getString("author"),
                            memeJSONObject.getInt("ups")
                        )
                        memeJsonResponses.add(requiredMemeFormat)
  }
                    mAdapter.updateMemes(memeJsonResponses)
                },
                {
//                    TODO
                }
            )
            MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)

    }


//  Open post in chrome custom tab, when image is clicked
    override fun memeImageClicked(item: MemeJsonResponse) {

        val builder = CustomTabsIntent.Builder()
        val colorInt: Int = Color.parseColor("#FF0000") //red
        builder.setToolbarColor(colorInt)
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, Uri.parse(item.postLink))


    }



    //provides sharing functionality of an image file type
    override fun shareImage(image: Drawable, isGif: Boolean) {

        val filePath: String = savetoDir(image, isGif)
        val context = this
        val file = File(filePath)
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "image/*"
        val uri = FileProvider.getUriForFile(context, "$packageName.provider", file)
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)
//        sharingIntent.putExtra(Intent.EXTRA_TEXT, curImageUrl)
        val intentChooser = Intent.createChooser(sharingIntent, "Share via")

//        val resInfoList = packageManager.queryIntentActivities(intentChooser, PackageManager.MATCH_DEFAULT_ONLY)
//
//        for (resolveInfo in resInfoList) {
//            val packageName = resolveInfo.activityInfo.packageName
//            grantUriPermission(
//                packageName, uri,
//                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
//                        Intent.FLAG_GRANT_READ_URI_PERMISSION
//            )
//        }

        context.startActivity(intentChooser)
    }


    override fun notYetLoaded() {
        Toast.makeText(this, "Please wait for the meme to load!", Toast.LENGTH_SHORT).show()
    }


    private val viewModel: MemeViewModel by viewModels {

        MemeViewModelFactory((application as MemeApplication).repository)

    }


    override fun saveMeme(savedMeme: SavedMeme) {
        viewModel.insert(savedMeme)
    }


//    Downloads image to DIR and returns the file path

    override fun savetoDir (image: Drawable, isGif: Boolean): String {

        if(isGif){
            val byteBuffer = (image as GifDrawable).buffer
            val fileName = "LitMemes${System.currentTimeMillis()}.gif"
            val filePath = "${this.cacheDir}/$fileName"
            val gifFile = File(filePath)
            val output = FileOutputStream(gifFile)
            val bytes = ByteArray(byteBuffer.capacity())

            (byteBuffer.duplicate().clear() as ByteBuffer).get(bytes)
            output.write(bytes, 0 ,bytes.size)
            output.close()

            return filePath
        }

        val fileName = "LitMemes${System.currentTimeMillis()}.png"
        val filePath = "${this.cacheDir}/$fileName"

        val file = File(filePath)
        FileOutputStream(file).use { output ->
            image.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, output)
        }

        return filePath

    }

    override fun deleteMeme(url: String) {
        viewModel.delete(url)
    }


}

