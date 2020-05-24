package com.olivejar.mealerts

import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.olivejar.mealerts.data.Sighting
import kotlinx.android.synthetic.main.card_content.view.*


class DetailAdapter(private val parentActivity: DetailActivity) : RecyclerView.Adapter<DetailAdapter.ViewHolder>() {
    private val mSightings = ArrayList<Sighting>()
    private var mByLoc = true

    fun setSightings(newSightings: List<Sighting>, byLoc:Boolean) {
        mByLoc=byLoc
        mSightings.clear()
        mSightings.addAll(newSightings)
        notifyDataSetChanged()
    }

    fun openWebPage(url: String) {
        val webpage = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        if (intent.resolveActivity(parentActivity.packageManager) != null) {
            parentActivity.applicationContext.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sighting = mSightings[position]
        var name = if (mByLoc) sighting.species else sighting.location
        holder.title.text = "${name} -- Count ${sighting.count}"
        holder.details.text = "${sighting.date} -- ${sighting.who}"
        holder.sighting = sighting

        with(holder.itemView) {
            tag = holder
            ibChecklist.tag = holder
            ibChecklist.setOnClickListener(onChecklistClickListener)
            if(mByLoc){
                ibMap.visibility=View.INVISIBLE
                ibMap.isEnabled=false
            }
            else {
                ibMap.visibility=View.VISIBLE
                ibMap.isEnabled=true
                ibMap.tag = holder
                ibMap.setOnClickListener(onMapClickListener)
            }
        }
    }

    override fun getItemCount() = mSightings.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var sighting: Sighting? = null
        val title: TextView = view.title
        val details: TextView = view.details
    }

    private val onChecklistClickListener: View.OnClickListener
    private val onMapClickListener: View.OnClickListener

    init {
        onChecklistClickListener = View.OnClickListener { v ->
            val holder = v.tag as DetailAdapter.ViewHolder
            val url = holder.sighting?.checklist ?: ""
            openWebPage(url)
        }
        onMapClickListener = View.OnClickListener { v ->
            val holder = v.tag as DetailAdapter.ViewHolder
            val url = holder.sighting?.map ?: ""
            openWebPage(url)
        }
    }
}