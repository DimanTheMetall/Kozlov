package com.example.kozlovdvtest

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.kozlovdvtest.databinding.ActivityMainBinding
import com.example.kozlovdvtest.retrofit.RetrofitService
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var mainActivityBinding: ActivityMainBinding
    private var gifURL: String? = null
    private var description: String? = null
    private var previewURL: String? = null

    private var descriptionsList: MutableList<String?> = mutableListOf()
    private var imageURLList: MutableList<String?> = mutableListOf()
    private var gifURLList: MutableList<String?> = mutableListOf()
    private var index = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainActivityBinding.root)

        mainActivityBinding.navigationMenu.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.tag) {
                    "last" -> {
                        mainActivityBinding.btnNextPost.setOnClickListener { nextRandomPost() }
                    }
                    "best" -> {
                        mainActivityBinding.btnNextPost.setOnClickListener { }
                    }
                    "hot" -> {
                        mainActivityBinding.btnNextPost.setOnClickListener { }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })

        mainActivityBinding.btnPreviousPost.setOnClickListener { previousPost() }
    }


    private fun renderState() {

        if (index == 0) {
            mainActivityBinding.btnPreviousPost.visibility = View.INVISIBLE
        } else {
            mainActivityBinding.btnPreviousPost.visibility = View.VISIBLE
        }

        mainActivityBinding.txtOnImage.text = descriptionsList[index]
        when (gifURLList[index]) {
            null -> {
                Glide
                    .with(this)
                    .load(imageURLList[index])
                    .into(mainActivityBinding.mainImageView)
                Log.d(Constants.IMAGE_TAG, "GifUrl is Null")
            }
            else -> {
                Glide
                    .with(this)
                    .asGif()
                    .placeholder(R.drawable.ic_loading)
                    .error(mainActivityBinding.mainImageView.visibility == View.INVISIBLE)
                    .load(gifURLList[index])
                    .into(mainActivityBinding.mainImageView)
                Log.d(Constants.IMAGE_TAG, "GifUrl is NotNull")
            }
        }
    }

    private fun previousPost() {
        index--
        renderState()
    }

    private fun nextRandomPost() {
        index++

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        GlobalScope.launch(Dispatchers.IO) {
            val resultFromDevLife = retrofit.create(RetrofitService::class.java)
            val onResponse = resultFromDevLife.getRandomPost().execute()

            gifURL = onResponse.body()!!.gifURL
            description = onResponse.body()!!.description
            previewURL = onResponse.body()!!.previewURL

            descriptionsList.add(description)
            imageURLList.add(previewURL)
            gifURLList.add(gifURL)

            withContext(Dispatchers.Main) {
                renderState()
            }
        }
    }
}