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
import com.example.kozlovdvtest.R
import com.example.kozlovdvtest.databinding.FragmentLastPostBinding
import com.example.kozlovdvtest.retrofit.DataFromDevLife
import com.example.kozlovdvtest.retrofit.JsonData
import com.example.kozlovdvtest.retrofit.RetrofitService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class PostFragment : Fragment() {
    lateinit var bindingLastPostFragment: FragmentLastPostBinding
    private var jsonList = mutableListOf<JsonData>()
    private var descriptionsList: MutableList<String?> = mutableListOf()
    private var imageURLList: MutableList<String?> = mutableListOf()
    private var gifURLList: MutableList<String?> = mutableListOf()
    private var index = -1
    private var sideIndex = -1

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

        nextPost()

        bindingLastPostFragment.btnNextPost.setOnClickListener { nextPost() }
        bindingLastPostFragment.btnPreviousPost.setOnClickListener { previousPost() }

        return bindingLastPostFragment.root
    }

    private fun renderState() {
        if (index > 0) {
            bindingLastPostFragment.btnPreviousPost.visibility = View.VISIBLE
        } else {
            bindingLastPostFragment.btnPreviousPost.visibility = View.INVISIBLE
        }

        bindingLastPostFragment.txtPage.text = getString(R.string.post_text, index + 1)
        bindingLastPostFragment.txtOnImage.text = descriptionsList.getOrNull(index)

        val gifUrl = gifURLList.getOrNull(index)
        val imageUrl = imageURLList.getOrNull(index)
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

    private fun previousPost() {
        changeIndex(false)
        renderState()
        getPageIndex()
    }

    private fun changeIndex(isNext: Boolean) {
        when (isNext) {
            true -> {
                index++
                sideIndex++
                if (sideIndex == 5) {
                    sideIndex = 0
                }
            }
            false -> {
                index--
                sideIndex--
                if (sideIndex == -1) {
                    sideIndex = 4
                }
            }
        }

        Log.d(Constants.DEBAG_TAG, "index: $index")
        Log.d(Constants.DEBAG_TAG, "sideIndex: $sideIndex")
    }

    private fun onLoading() {
        bindingLastPostFragment.progressCircular.isVisible = true
        bindingLastPostFragment.txtError.isVisible = false
        bindingLastPostFragment.mainImageView.isVisible = false
    }

    private fun getPageIndex(): String {
        val pageIndex = index / 4
        Log.d(Constants.DEBAG_TAG, "${Constants.POST_PAGE}: $pageIndex")
        return "$pageIndex"
    }

    private fun addInList(listOfJson: MutableList<JsonData>) {
        val gifURL = listOfJson[sideIndex].gifURL
        val description = listOfJson[sideIndex].description
        val previewURL = listOfJson[sideIndex].previewURL
        if (index == descriptionsList.size) {
            descriptionsList.add(description)
            imageURLList.add(previewURL)
            gifURLList.add(gifURL)
            Log.d(Constants.DEBAG_TAG, "Data add in list")
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private fun nextPost() {

        CoroutineScope(Dispatchers.Main).launch {

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
    }

    private suspend fun load(onLoad: suspend () -> Response<DataFromDevLife>) {
        try {
            changeIndex(true)
            if (sideIndex == 0) {

                val onResponse = withContext(Dispatchers.IO) {
                    onLoad.invoke()
                }.body()
                jsonList = onResponse?.result?.toMutableList()!!

                addInList(jsonList)

                renderState()
            } else {

                addInList(jsonList)

                renderState()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            bindingLastPostFragment.progressCircular.isVisible = false
            bindingLastPostFragment.txtError.isVisible = true
            bindingLastPostFragment.mainImageView.isVisible = false

            renderState()
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