package com.olivejar.mealerts

import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.google.gson.Gson
import com.olivejar.mealerts.data.Alert
import com.olivejar.mealerts.data.Sighting
import com.olivejar.mealerts.data.Summary
import com.olivejar.mealerts.database.database
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_summary.*
import kotlinx.android.synthetic.main.content_summary.*
import org.jetbrains.anko.toast

class SummaryActivity : AppCompatActivity() {

    companion object {
        const val ARG_ALERT_GSON = "Extra_Alert"
        public enum class Mode {
            BY_LOC, BY_SPECIES, NEAREST
        }
    }

    private lateinit var mSummarySubscription: Disposable
    private lateinit var mSummaryAdapter: SummaryAdapter
    private lateinit var mAlert: Alert

    private var mMode = Mode.BY_SPECIES

    fun getSummaries(): ArrayList<Summary> {
        var results = ArrayList<Summary>()
        var x = Sighting.getSightings(database, mAlert.id)
        if (mAlert.id >= 0) {
            when (mMode) {
                Mode.BY_SPECIES -> {
                    results = Summary.getSummariesBySpecies(database, mAlert.id)
                }
                Mode.BY_LOC -> {
                    results = Summary.getSummariesByLocation(database, mAlert.id)
                }
                Mode.NEAREST -> {
                    var sightings = Sighting.getSightings(database, mAlert.id)
                    var locMgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    try {
                        var loc = locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        results = Summary.getClosest(database, mAlert.id, loc)
                    } catch (e: SecurityException) {
                        toast("Could not get location").show()
                    }
                }
            }
        }
        return results
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent != null) {
            intent?.let {
                if (it.hasExtra(ARG_ALERT_GSON)) {
                    val gson = it.getStringExtra(ARG_ALERT_GSON)
                    mAlert = Gson().fromJson(gson, Alert::class.java)
                } else {
                    mAlert = Alert(-1, "1970-01-01", "00:00", 0, "Unknown")
                }
            }
        } else {
            mAlert = Alert(-1, "1970-01-01", "00:00", 0, "Unknown")
        }

        setContentView(R.layout.activity_summary)
        setSupportActionBar(toolbarSummary)
        var title = mAlert.name
        title = title.replace("Alert ", "")
        title = title.substring(0, title.indexOf(" <"))
        supportActionBar?.title = title
        // Show the Up button in the action bar.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        mSummaryAdapter = SummaryAdapter(this)
        summary_list.adapter = mSummaryAdapter
    }

    override fun onResume() {
        super.onResume()
        val recordObservable: Observable<ArrayList<Summary>> = Observable
                .fromCallable({ getSummaries() })
        mSummarySubscription = recordObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ rList -> mSummaryAdapter.setSummaries(rList, mMode) })
    }

    override fun onStop() {
        if (!mSummarySubscription.isDisposed) {
            mSummarySubscription.dispose()
        }
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_summary, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null) {
            when (item.getItemId()) {
                android.R.id.home -> {
                    this.onBackPressed()
                    return true
                }
                R.id.action_pivot -> {
                    mMode = if (mMode == Mode.BY_SPECIES) Mode.BY_LOC else Mode.BY_SPECIES
                    mSummaryAdapter.setSummaries(getSummaries(), mMode )
                }
                R.id.action_nearest -> {
                    mMode = Mode.NEAREST
                    mSummaryAdapter.setSummaries(getSummaries(), mMode )
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
