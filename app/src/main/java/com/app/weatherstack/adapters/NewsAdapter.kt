package com.app.weatherstack.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.weatherstack.NewsArticles
import com.app.weatherstack.R
import com.app.weatherstack.utils.DateTimeValsUtils
import com.bumptech.glide.Glide

class NewsAdapter(private val itemClickListener: NewsAdapter.OnItemClickListener) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    private var listOfNews = listOf<NewsArticles>()

    interface OnItemClickListener {
        fun onItemClick(article: NewsArticles)
    }

    class NewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val newsDetailsSource   : TextView  = view.findViewById(R.id.newsDetailsSource)
        val newsDetailsTitle    : TextView  = view.findViewById(R.id.newsDetailsTitle)
        val newsDetailsTimer    : TextView  = view.findViewById(R.id.newsDetailsTimePosted)
        val newsDetailsImage    : ImageView = view.findViewById(R.id.newsDetailsImage)
        val shareButton         : ImageView = view.findViewById(R.id.shareIcon)
        val shareTextView       : TextView  = view.findViewById(R.id.shareText)

        fun bind(article: NewsArticles, clickListener: OnItemClickListener) {

            Glide.with(newsDetailsImage.context)
                .load(article.urlToImage)
                .into(newsDetailsImage)

            newsDetailsTitle.text = article.title
            newsDetailsTimer.text = DateTimeValsUtils.getTimeAgo(article.publishedAt!!)
            newsDetailsSource.text= article.source?.name

            itemView.setOnClickListener {
                clickListener.onItemClick(article)
            }

            shareButton.setOnClickListener {
                shareNews(article.url!!, shareButton.context)
            }
            shareTextView.setOnClickListener {
                shareNews(article.url!!, shareTextView.context)
            }
        }
    }

    fun setList(newList:List<NewsArticles>){
        this.listOfNews=newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return listOfNews.size
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.news_element, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val newsObject = listOfNews[position]

        holder.bind(newsObject,itemClickListener)
    }

}

fun shareNews(url: String, context: Context) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "Check out this news: $url")
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
}