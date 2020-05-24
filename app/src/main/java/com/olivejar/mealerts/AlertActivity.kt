package com.olivejar.mealerts

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.*
import com.olivejar.mealerts.data.Alert
import com.olivejar.mealerts.database.database
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_alert.*
import kotlinx.android.synthetic.main.content_alert.*
import org.jetbrains.anko.startActivity

/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [AlertSummaryActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class AlertActivity : AppCompatActivity() {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private lateinit var mAlertSubscription: Disposable
    private lateinit var mAlertAdapter: AlertAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert)

        setSupportActionBar(toolbarAlert)
        toolbarAlert.title = title

        mAlertAdapter = AlertAdapter(this)
        alert_list.adapter = mAlertAdapter
    }

    override fun onResume() {
        super.onResume()
        val recordObservable: Observable<ArrayList<Alert>> = Observable
                .fromCallable({ Alert.getAlerts(database) })
        mAlertSubscription = recordObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ rList -> mAlertAdapter.setAlerts(rList) })
    }

    override fun onStop() {
        if (!mAlertSubscription.isDisposed) {
            mAlertSubscription.dispose()
        }
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_alert, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_parsemail -> {
                startActivity<ParseActivity>()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
