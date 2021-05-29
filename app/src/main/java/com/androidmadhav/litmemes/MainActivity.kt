package com.androidmadhav.litmemes

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    private var curImageUrl: String? = null

    lateinit var imageBitMap: Bitmap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadMeme()
    }

    

    private fun loadMeme(){
        val url = "https://meme-api.herokuapp.com/gimme"
        progressBar.visibility = View.VISIBLE
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
               response.getString("url").also { curImageUrl = it }
                Glide.with(this).asBitmap().load(curImageUrl).listener(object :
                    RequestListener<Bitmap> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap,
                        model: Any?,
                        target: Target<Bitmap>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        imageBitMap = resource
                        progressBar.visibility = View.GONE
                        return false
                    }

                }).into(memeImage)

            }


        ) { error ->
            Toast.makeText(this, "Image Loading Failed! Please try after sometime.", Toast.LENGTH_SHORT).show()
        }

        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)


    }

    //download the image in cache
    private fun downloadImageThenShare(bitmap: Bitmap) {
        val fileName = "LitMemes${System.currentTimeMillis()}.png"
        val filePath = "${this.cacheDir}/$fileName"
        downloadImageIntoCache(bitmap, filePath) {
            shareImage(this, File(filePath))
        }
    }

    //download .png file
    private fun downloadImageIntoCache(bitmap: Bitmap, path: String, finishDownload: () -> Unit) {
        val file = File(path)
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            finishDownload.invoke()
        }
    }

    //provides sharing functionality of an image file type
    private fun shareImage(context: Context, file: File) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "image/*"
        val uri = FileProvider.getUriForFile(context, "$packageName.provider", file)
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)
        sharingIntent.putExtra(Intent.EXTRA_TEXT, curImageUrl)
        val intentChooser = Intent.createChooser(sharingIntent, "Share via")

        val resInfoList =
            packageManager.queryIntentActivities(intentChooser, PackageManager.MATCH_DEFAULT_ONLY)

        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            grantUriPermission(
                packageName, uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        context.startActivity(intentChooser)
    }

    fun nextMeme(view: View) {
        loadMeme()
    }


    fun shareMeme(view: View) {

        downloadImageThenShare(imageBitMap)
    }


}


