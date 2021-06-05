package com.androidmadhav.litmemes

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.load.resource.gif.GifDrawable
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer


class MainActivity : AppCompatActivity(), AddOns {


    private var mAdapter = MemeListAdapter(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MemeRecyclerView.layoutManager = LinearLayoutManager(this)

        fetchdata()

        MemeRecyclerView.adapter = mAdapter


    }

    override fun onStop() {
        super.onStop()
    }

    //Fires after the OnStop() state
    override fun onDestroy() {
        super.onDestroy()
        try {
            trimCache(this)
        } catch (e: Exception) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    fun trimCache(context: Context) {
        try {
            val dir = context.cacheDir
            if (dir != null && dir.isDirectory) {
                deleteDir(dir)
            }
        } catch (e: Exception) {
            // TODO: handle exception
        }
    }

    fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
        }

        // The directory is now empty so delete it
        return dir!!.delete()
    }

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

    override fun onItemClicked(item: MemeJsonResponse) {

        val builder = CustomTabsIntent.Builder()
        val colorInt: Int = Color.parseColor("#FF0000") //red
        builder.setToolbarColor(colorInt)
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, Uri.parse(item.postLink))


    }


//
    //download the image in cache
    private fun downloadImageThenShare(imageDrawable: Drawable) {
        val fileName = "LitMemes${System.currentTimeMillis()}.png"
        val filePath = "${this.cacheDir}/$fileName"
        downloadImageIntoCache(imageDrawable, filePath) {
            shareImage(this, File(filePath))
        }
    }

    //download .png file
    private fun downloadImageIntoCache(imageDrawable: Drawable, path: String, finishDownload: () -> Unit) {
        val file = File(path)
        FileOutputStream(file).use { output ->
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            imageDrawable.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, output)
            finishDownload.invoke()
        }
    }

    //provides sharing functionality of an image file type
    private fun shareImage(context: Context, file: File) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "image/*"
        val uri = FileProvider.getUriForFile(context, "$packageName.provider", file)
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)
//        sharingIntent.putExtra(Intent.EXTRA_TEXT, curImageUrl)
        val intentChooser = Intent.createChooser(sharingIntent, "Share via")

//        val resInfoList =
//            packageManager.queryIntentActivities(intentChooser, PackageManager.MATCH_DEFAULT_ONLY)
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




    override fun shareImg(image: Drawable) {

        downloadImageThenShare(image)
    }

    override fun shareGif(image: Drawable) {

        val byteBuffer = (image as GifDrawable).buffer
        val fileName = "LitMemes${System.currentTimeMillis()}.gif"
        val filePath = "${this.cacheDir}/$fileName"
        val gifFile = File(filePath)

        val output = FileOutputStream(gifFile)
        val bytes = ByteArray(byteBuffer.capacity())
        (byteBuffer.duplicate().clear() as ByteBuffer).get(bytes)
        output.write(bytes, 0 ,bytes.size)
        shareImage(this, File(filePath))

        output.close()

    }


}

