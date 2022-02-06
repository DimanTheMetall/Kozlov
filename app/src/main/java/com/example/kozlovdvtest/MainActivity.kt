package com.example.kozlovdvtest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.kozlovdvtest.databinding.ActivityMainBinding
import com.example.kozlovdvtest.fragment.PostFragment
import com.google.android.material.tabs.TabLayout


class MainActivity : AppCompatActivity() {
    private lateinit var mainActivityBinding: ActivityMainBinding

    private val fragmentLast by lazy { PostFragment.newInstance(Constants.CATEGORY_LATEST) }
    private val fragmentBest by lazy { PostFragment.newInstance(Constants.CATEGORY_BEST) }
    private val fragmentHot by lazy { PostFragment.newInstance(Constants.CATEGORY_HOT) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainActivityBinding.root)
        replaceFrag(fragmentLast)

        mainActivityBinding.navigationMenu.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.text) {
                    getString(R.string.last_post) -> {
                        replaceFrag(fragmentLast)
                    }
                    getString(R.string.best_post) -> {
                        replaceFrag(fragmentBest)
                    }
                    getString(R.string.hot_post) -> {
                        replaceFrag(fragmentHot)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })
    }

    fun replaceFrag(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_view_holder, fragment)
            .commit()
    }
}