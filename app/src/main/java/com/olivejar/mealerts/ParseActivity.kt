package com.olivejar.mealerts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import com.olivejar.mealerts.data.AlertIntentService

import kotlinx.android.synthetic.main.activity_parse.*
import kotlinx.android.synthetic.main.content_parse.*
import java.util.*

class ParseActivity : AppCompatActivity() {

    private val alertListener = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1 != null) {
                when (p1.action) {
                    AlertIntentService.ACTION_UPDATE_COMPLETE -> {
                        progressBarP.isIndeterminate = false
                        textViewStatus.append("Update Complete ${commonDateTime.format(Calendar.getInstance().time)}\n")
                        textViewStatus.append(" \n")
                        scrollViewStatus.pageScroll(View.FOCUS_DOWN)
                    }
                    AlertIntentService.ACTION_ALERT_UPDATE -> {
                        if (p1.hasExtra(AlertIntentService.EXTRA_MESSAGE)) {
                            var msg = p1.getStringExtra(AlertIntentService.EXTRA_MESSAGE)
                            textViewStatus.append("${msg}\n")
                            scrollViewStatus.fullScroll(View.FOCUS_DOWN)
                        }
                    }
                    AlertIntentService.ACTION_ALERTS_CLEARED -> {
                        textViewStatus.append("Alerts Cleared\n")
                        progressBarP.isIndeterminate = false
                    }
                }
            }
        }
    }

    private val buttonListener = View.OnClickListener { _ ->
        progressBarP.isIndeterminate = true
        textViewStatus.text=commonDateTime.format(Calendar.getInstance().time) + "\n"
        if (checkBoxClr.isChecked) {
            AlertIntentService.ClearAlerts(this)
        }
        AlertIntentService.updateAlerts(this)
    }

    val commonDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parse)
        setSupportActionBar(toolbarParse)
        // Show the Up button in the action bar.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        registerReceiver(alertListener, IntentFilter(AlertIntentService.ACTION_ALERT_UPDATE))
        registerReceiver(alertListener, IntentFilter(AlertIntentService.ACTION_UPDATE_COMPLETE))
        registerReceiver(alertListener, IntentFilter(AlertIntentService.ACTION_ALERTS_CLEARED))

        buttonParse.setOnClickListener(buttonListener)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null) {
            when (item.getItemId()) {
                android.R.id.home -> {
                    this.onBackPressed()
                    return true
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        unregisterReceiver(alertListener)
        super.onDestroy()
    }
}
