package com.app.weatherstack.news


import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.app.weatherstack.NewsArticles
import com.app.weatherstack.R
import com.app.weatherstack.adapters.NewsAdapter
import com.app.weatherstack.repositories.NewsRepository
import com.app.weatherstack.utils.Constants
import com.app.weatherstack.viewmodelfactories.NewsViewModelFactory
import com.app.weatherstack.viewmodels.NewsViewModel



class NewsActivity : AppCompatActivity(), NewsAdapter.OnItemClickListener {

    private lateinit var toolbar            : Toolbar
    private lateinit var newsRV             : RecyclerView
    private lateinit var newsAdapter        : NewsAdapter
    private lateinit var backArrowNews      : ImageView
    private lateinit var availableCountriesForNews: List<String>
    private lateinit var newsViewModel: NewsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)

        hideSystemUI()
        initUI()

        val newsRepository = NewsRepository()
        val viewModelFactory = NewsViewModelFactory(newsRepository)
        newsViewModel = ViewModelProvider(this, viewModelFactory).get(NewsViewModel::class.java)

        availableCountriesForNews = listOf("ae","ar","at","au","be","bg","br","ca","ch",
            "cn","co","cu","cz","de","eg","fr","gb","gr","hk","hu","id","ie","il","in","it","jp","kr","lt","lv","ma",
            "mx","my","ng","nl","no","nz","ph","pl","pt","ro","rs","ru","sa","se","sg","si","sk","th","tr","tw","ua","us","ve","za")

        if(getLastLocationFromSharedPrefs()!="null" && availableCountriesForNews.contains(getLastLocationFromSharedPrefs().lowercase()))
            newsViewModel.getTopHeadlines(getLastLocationFromSharedPrefs(), Constants.newsAPIKey)
        else
            newsViewModel.getTopHeadlines("us", Constants.newsAPIKey)

        newsViewModel.news.observe(this, Observer{ response ->
                    if (response!=null){
                    Log.d("NewsResponse","$response")

                    val newsList = mutableListOf<NewsArticles>()

                    for(article in response.articles) {

                        if(article.source?.name!="[Removed]" && article.title!="[Removed]" && article.description!="[Removed]"&& article.content!="[Removed]")
                        {
                            newsList.add(article)
                        }
                    }

                    newsAdapter.setList(newsList)


            }
        })
    }

    private fun initUI() {
        toolbar             = findViewById(R.id.newsToolbar)
        setSupportActionBar(toolbar)
        newsRV              = findViewById(R.id.newsRecyclerView)
        newsAdapter         = NewsAdapter(this)
        newsRV.adapter      = newsAdapter
        backArrowNews       = findViewById(R.id.backArrowDarkNews)
        backArrowNews.setOnClickListener {
            finish()
        }

    }

    private fun getLastLocationFromSharedPrefs():String {
        val sharedPreferences = getSharedPreferences("LastCountrySearchedOrSelected", Context.MODE_PRIVATE)
        val lastCountry = sharedPreferences.getString("LastCountry", null)
//        Toast.makeText(this,"LastCountry $lastCountry", Toast.LENGTH_SHORT).show()
        return lastCountry ?: "Bucharest"
    }

    private fun hideSystemUI() {
        val decorView: View = window.decorView

        // Hide both the navigation bar and the status bar.
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        decorView.systemUiVisibility = uiOptions
    }

    override fun onItemClick(article: NewsArticles) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(article.url)
        }
        startActivity(Intent.createChooser(intent, "Choose browser"))

    }
}