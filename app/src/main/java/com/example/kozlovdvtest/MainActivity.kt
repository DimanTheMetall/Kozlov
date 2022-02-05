package com.example.kozlovdvtest

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.kozlovdvtest.databinding.ActivityMainBinding
import com.example.kozlovdvtest.retrofit.RetrofitService
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private lateinit var mainActivityBinding: ActivityMainBinding

    private var descriptionsList: MutableList<String?> = mutableListOf()
    private var imageURLList: MutableList<String?> = mutableListOf()
    private var gifURLList: MutableList<String?> = mutableListOf()
    private var index = -1

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val retrofitService = retrofit.create(RetrofitService::class.java)

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

        mainActivityBinding.btnNextPost.setOnClickListener { nextRandomPost() }
        mainActivityBinding.btnPreviousPost.setOnClickListener { previousPost() }
        nextRandomPost()
    }


    private fun renderState() {
        if (index > 0) {
            mainActivityBinding.btnPreviousPost.visibility = View.VISIBLE
        } else {
            mainActivityBinding.btnPreviousPost.visibility = View.INVISIBLE
        }

        mainActivityBinding.txtOnImage.text = descriptionsList.getOrNull(index)

        val gifUrl = gifURLList.getOrNull(index)
        val imageUrl = imageURLList.getOrNull(index)
        when {
            gifUrl != null -> load(gifUrl, true)
            imageUrl != null -> load(imageUrl, false)
        }
    }

    private fun load(url: String, isGif: Boolean) {
        mainActivityBinding.txtError.isVisible = false
        mainActivityBinding.progressCircular.isVisible = true
        mainActivityBinding.mainImageView.isVisible = true
        mainActivityBinding.mainImageView.setImageDrawable(null)
        if (isGif) {
            Glide
                .with(this)
                .asGif()
                .load(url)
                .addListener(listener())
                .into(mainActivityBinding.mainImageView)
        } else {
            Glide
                .with(this)
                .load(url)
                .addListener(listener())
                .into(mainActivityBinding.mainImageView)
        }

    }

    private fun <T> listener() =
        object : RequestListener<T> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<T>?,
                isFirstResource: Boolean
            ): Boolean {
                mainActivityBinding.progressCircular.isVisible = false
                mainActivityBinding.txtError.isVisible = true
                return false
            }

            override fun onResourceReady(
                resource: T,
                model: Any?,
                target: Target<T>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                mainActivityBinding.progressCircular.isVisible = false
                mainActivityBinding.txtError.isVisible = false
                return false
            }
        }

    private fun previousPost() {
        index--
        renderState()
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private fun nextRandomPost() {
        launch {
            mainActivityBinding.progressCircular.isVisible = true
            mainActivityBinding.txtError.isVisible = false
            mainActivityBinding.mainImageView.isVisible = false
            try {
                val onResponse = withContext(Dispatchers.IO) {
                    retrofitService.getRandomPost().execute()
                }.body()
                val gifURL = onResponse?.gifURL
                val description = onResponse?.description
                val previewURL = onResponse?.previewURL

                index++

                descriptionsList.add(index, description)
                imageURLList.add(index, previewURL)
                gifURLList.add(index, gifURL)

                renderState()
            } catch (e: Exception) {
                mainActivityBinding.progressCircular.isVisible = false
                mainActivityBinding.txtError.isVisible = true
                mainActivityBinding.mainImageView.isVisible = false
                renderState()
            }
        }
    }
}