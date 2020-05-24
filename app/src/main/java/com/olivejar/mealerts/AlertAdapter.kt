package com.olivejar.mealerts

import android.content.Intent
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.gson.Gson
import com.olivejar.mealerts.data.Alert
import kotlinx.android.synthetic.main.item_content.view.*
import org.jetbrains.anko.backgroundColor

class AlertAdapter(private val parentActivity: AlertActivity) : RecyclerView.Adapter<AlertAdapter.ViewHolder>() {
    private val mAlerts = ArrayList<Alert>()

    fun setAlerts(newAlerts: List<Alert>) {
        mAlerts.clear()
        mAlerts.addAll(newAlerts)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alert = mAlerts[position]
        holder.title.text = alert.name
        holder.details.text = alert.date
        holder.alert = alert

        with(holder.itemView) {
            tag = holder
            setOnClickListener(onClickListener)
        }
    }

    override fun getItemCount() = mAlerts.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var alert: Alert? = null
        val title: TextView = view.title
        val details: TextView = view.details
    }

    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { v ->
            val holder = v.tag as ViewHolder
            val alertGson = Gson().toJson(holder.alert)

            val intent = Intent(v.context, SummaryActivity::class.java).apply {
                putExtra(SummaryActivity.ARG_ALERT_GSON, alertGson)
            }
            v.context.startActivity(intent)
        }
    }
}