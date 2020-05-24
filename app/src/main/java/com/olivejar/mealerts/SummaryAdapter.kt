package com.olivejar.mealerts

import android.content.Intent
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.gson.Gson
import com.olivejar.mealerts.data.Summary
import kotlinx.android.synthetic.main.item_content.view.*
import org.jetbrains.anko.backgroundColor

class SummaryAdapter(private val parentActivity: SummaryActivity) : RecyclerView.Adapter<SummaryAdapter.ViewHolder>() {
    private val mSummaries = ArrayList<Summary>()
    private var mMode = SummaryActivity.Companion.Mode.BY_SPECIES

    fun setSummaries(newSummaries: List<Summary>, mode:SummaryActivity.Companion.Mode) {
        mMode=mode
        mSummaries.clear()
        mSummaries.addAll(newSummaries)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val summary = mSummaries[position]
        holder.title.text = summary.name
        holder.details.text = "${summary.date} -- (${summary.count})"
        holder.summary = summary

        with(holder.itemView) {
            tag = holder
            setOnClickListener(onClickListener)
        }
    }

    override fun getItemCount() = mSummaries.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var summary: Summary? = null
        val title: TextView = view.title
        val details: TextView = view.details
    }

    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { v ->
            val holder = v.tag as ViewHolder
            val summaryGson = Gson().toJson(holder.summary)

            val intent = Intent(v.context, DetailActivity::class.java).apply {
                putExtra(DetailActivity.ARG_SUMMARY_GSON, summaryGson)
                putExtra(DetailActivity.ARG_BY_LOC,mMode!=SummaryActivity.Companion.Mode.BY_SPECIES)
            }
            v.context.startActivity(intent)
        }
    }
}