package com.androidmadhav.litmemes

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidmadhav.litmemes.databinding.ActivitySavedMemesBinding

class SavedMemesActivity : MainActivity(){

    private val viewModel: MemeViewModel by viewModels{

        MemeViewModelFactory((application as MemeApplication).repository)

    }

    private lateinit var binding: ActivitySavedMemesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedMemesBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_saved_memes)

        val recyclerView: RecyclerView = findViewById(R.id.savedMemesRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter: SavedMemesAdapter = SavedMemesAdapter(this)

        recyclerView.adapter = adapter

        viewModel.allSavedMeme.observe(this, Observer {

//            "It" is allSavedMemes
            adapter.updateList(it)

        })

        binding.floatingActionButton.setOnClickListener({
            val intent = Intent(this, MainActivity::class.java)
//          Brings the activity to front w/o creating again
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        })


    }
}