package com.example.kozlovdvtest.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.kozlovdvtest.Constants
import com.example.kozlovdvtest.Constants.POST_COUNT
import com.example.kozlovdvtest.R
import com.example.kozlovdvtest.databinding.FragmentLastPostBinding
import com.example.kozlovdvtest.fillNullsToIndex
import com.example.kozlovdvtest.retrofit.DataFromDevLife
import com.example.kozlovdvtest.retrofit.JsonData
import com.example.kozlovdvtest.retrofit.RetrofitService
import kotlinx.coroutines.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class PostFragment : Fragment(), CoroutineScope by MainScope() {
    lateinit var bindingLastPostFragment: FragmentLastPostBinding
    private var jsonList = mutableListOf<JsonData?>()
    private var index = 0

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val retrofitService = retrofit.create(RetrofitService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bindingLastPostFragment = FragmentLastPostBinding.inflate(inflater, container, false)

        launch {
            loadPost()
        }

        bindingLastPostFragment.btnNextPost.setOnClickListener { nextPost() }
        bindingLastPostFragment.btnPreviousPost.setOnClickListener { previousPost() }

        return bindingLastPostFragment.root
    }

    private fun renderState() {

        val item = jsonList.getOrNull(index)

        if (index > 0) {
            bindingLastPostFragment.btnPreviousPost.visibility = View.VISIBLE
        } else {
            bindingLastPostFragment.btnPreviousPost.visibility = View.INVISIBLE
        }

        bindingLastPostFragment.txtPage.text = getString(R.string.post_text, index + 1)
        bindingLastPostFragment.txtOnImage.text = item?.description

        val gifUrl = item?.gifURL
        val imageUrl = item?.previewURL
        when {
            gifUrl != null -> load(gifUrl, true)
            imageUrl != null -> load(imageUrl, false)
        }
    }

    private fun load(url: String, isGif: Boolean) {
        bindingLastPostFragment.txtError.isVisible = false
        bindingLastPostFragment.progressCircular.isVisible = true
        bindingLastPostFragment.mainImageView.isVisible = true
        bindingLastPostFragment.mainImageView.setImageDrawable(null)
        if (isGif) {
            Glide
                .with(this)
                .asGif()
                .load(url)
                .addListener(listener())
                .into(bindingLastPostFragment.mainImageView)
        } else {
            Glide
                .with(this)
                .load(url)
                .addListener(listener())
                .into(bindingLastPostFragment.mainImageView)
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
                bindingLastPostFragment.progressCircular.isVisible = false
                bindingLastPostFragment.txtError.isVisible = true
                return false
            }

            override fun onResourceReady(
                resource: T,
                model: Any?,
                target: Target<T>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                bindingLastPostFragment.progressCircular.isVisible = false
                bindingLastPostFragment.txtError.isVisible = false
                return false
            }
        }

    private fun changeIndex(isNext: Boolean) {
        when (isNext) {
            true -> index++
            false -> index--
        }
        Log.d(Constants.DEBAG_TAG, "index: $index")
    }

    private fun onLoading() {
        bindingLastPostFragment.progressCircular.isVisible = true
        bindingLastPostFragment.txtError.isVisible = false
        bindingLastPostFragment.mainImageView.isVisible = false
    }

    private fun getPageIndex(): String = (index / POST_COUNT).toString()

    private fun previousPost() {
        launch {
            changeIndex(false)
            loadPost()
        }
    }

    private fun nextPost() {
        launch {
            changeIndex(true)
            loadPost()
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun loadPost() {
        val postCategory = requireArguments().getString(Constants.CATEGORY_KEY)

        onLoading()

        when (postCategory) {
            Constants.CATEGORY_LATEST -> {
                load {
                    retrofitService.getLatestPost(getPageIndex()).execute()
                }
            }
            Constants.CATEGORY_BEST -> {
                load {
                    retrofitService.getBestPost(getPageIndex()).execute()
                }
            }
            Constants.CATEGORY_HOT -> {
                load {
                    retrofitService.getHotPost(getPageIndex()).execute()
                }
            }
        }
    }

    private suspend fun load(onLoad: suspend () -> Response<DataFromDevLife>) {
        Log.d(Constants.DEBAG_TAG, "${Constants.POST_PAGE}: $index")
        try {
            if (jsonList.getOrNull(index) == null) {
                Log.d(Constants.DEBAG_TAG, "NOT LOADED. LOAD AT PAGE INDEX ${getPageIndex()}")
                val onResponse = withContext(Dispatchers.IO) {
                    onLoad.invoke()
                }.body()
                jsonList.fillNullsToIndex(index)
                // index / POST_COUNT round down value
                jsonList.addAll((index / POST_COUNT) * POST_COUNT, onResponse?.result ?: listOf())

                renderState()
            } else {
                Log.d(Constants.DEBAG_TAG, "ALREADY LOADED. RENDER")
                renderState()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            renderState()
            bindingLastPostFragment.progressCircular.isVisible = false
            bindingLastPostFragment.txtError.isVisible = true
            bindingLastPostFragment.mainImageView.isVisible = false
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(category: String): PostFragment {
            val fragment = PostFragment()
            val arguments = Bundle()
            arguments.putString(Constants.CATEGORY_KEY, category)
            fragment.arguments = arguments
            return fragment
        }
    }
}