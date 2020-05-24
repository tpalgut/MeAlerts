package com.olivejar.mealerts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import com.google.gson.Gson
import com.olivejar.mealerts.data.Sighting
import com.olivejar.mealerts.data.Summary
import com.olivejar.mealerts.database.database
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*
import kotlin.math.sign

class DetailActivity : AppCompatActivity() {

    companion object {
        const val ARG_SUMMARY_GSON = "Summary"
        const val ARG_BY_LOC = "ByLoc"
    }

    private lateinit var mDetailSubscription: Disposable
    private lateinit var mDetailAdapter: DetailAdapter
    private lateinit var mSummary: Summary
    private var mByLoc: Boolean = true
    private var mSighting =Sighting()

    fun getSightings(): ArrayList<Sighting> {
        var results = ArrayList<Sighting>()
        if (mSummary.alertId >= 0) {
            if (mByLoc) {
                results = Sighting.getSightingsByLocation(database, mSummary.alertId, mSummary.name)
                mSighting= if (results.count()>0)results[0] else Sighting()
            } else {
                results = Sighting.getSightingsBySpecies(database, mSummary.alertId, mSummary.name)
            }
        }
        return results
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent != null) {
            intent?.let {
                if (it.hasExtra(DetailActivity.ARG_SUMMARY_GSON)) {
                    val gson = it.getStringExtra(DetailActivity.ARG_SUMMARY_GSON)
                    mSummary = Gson().fromJson(gson, Summary::class.java)
                } else {
                    mSummary = Summary("Unknown", "1970-0101", 0, -1)
                }
                if (it.hasExtra(DetailActivity.ARG_BY_LOC)) {
                    mByLoc = it.getBooleanExtra(ARG_BY_LOC, true)
                }
            }
        } else {
            mSummary = Summary("Unknown", "1970-0101", 0, -1)
            mByLoc = true
        }

        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbarDetail)
        // Show the Up button in the action bar.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        supportActionBar?.title = mSummary.name
        textViewDate.text = "Last Report ${mSummary.date}"
        textViewCount.text = if (mByLoc) "Total Species ${mSummary.count}" else "Total Locations ${mSummary.count}"
        if (mByLoc) {
            ibMap.setOnClickListener { view ->
                val webpage = Uri.parse(mSighting.map)
                val intent = Intent(Intent.ACTION_VIEW, webpage)
                if (intent.resolveActivity(packageManager) != null) {
                    applicationContext.startActivity(intent)
                }

            }
        } else {
            ibMap.setOnClickListener { view ->
                Snackbar.make(view, "Map All Locations", Snackbar.LENGTH_LONG)
                        .setAction("Action_Map_All", null).show()
            }
        }

        mDetailAdapter = DetailAdapter(this)
        detail_list.adapter = mDetailAdapter
    }

    override fun onResume() {
        super.onResume()
        val recordObservable: Observable<ArrayList<Sighting>> = Observable
                .fromCallable({ getSightings() })
        mDetailSubscription = recordObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ rList -> mDetailAdapter.setSightings(rList, mByLoc) })
    }

    override fun onStop() {
        if (!mDetailSubscription.isDisposed) {
            mDetailSubscription.dispose()
        }
        super.onStop()
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
}
