package com.olivejar.mealerts

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.gmail.GmailScopes
import com.olivejar.mealerts.database.MeDbHelper
import kotlinx.android.synthetic.main.activity_alert_list.*

import kotlinx.android.synthetic.main.activity_splash.*
import org.jetbrains.anko.startActivity
import pub.devrel.easypermissions.EasyPermissions
import java.util.*

class SplashActivity : AppCompatActivity() , EasyPermissions.PermissionCallbacks {
    companion object {

        internal val REQUEST_ACCOUNT_PICKER = 1000
        internal val REQUEST_AUTHORIZATION = 1001
        internal val REQUEST_GOOGLE_PLAY_SERVICES = 1002
        internal val REQUEST_PERMISSION_GET_ACCOUNTS = 1003

        public val PREFS_FILE = "com.olivejar.mealerts"
        public val PREF_ACCOUNT_NAME = "accountName"
        public val PREF_PERMISSIONS_OK = "permissionsOK"
        public val SCOPES = arrayOf(GmailScopes.GMAIL_READONLY)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>?) {
        verifyPermissions()
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private val isDeviceOnline: Boolean
        get() {
            val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connMgr.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private val isGooglePlayServicesAvailable: Boolean
        get() {
            val apiAvailability = GoogleApiAvailability.getInstance()
            val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
            return connectionStatusCode == ConnectionResult.SUCCESS
        }

    internal lateinit var mCredential: GoogleAccountCredential
    internal lateinit var mPrefs: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        mPrefs = getSharedPreferences(PREFS_FILE,0)

        mCredential = GoogleAccountCredential.usingOAuth2(
                applicationContext, Arrays.asList(*SplashActivity.SCOPES))
                .setBackOff(ExponentialBackOff())
    }

    override fun onResume() {
        super.onResume()
        val permissionsOK = mPrefs.getBoolean(SplashActivity.PREF_PERMISSIONS_OK, false)
        if (!permissionsOK) {
            verifyPermissions()
        }
        else
        {
            startMainActivity()
        }
    }

    private fun startMainActivity(){
        initDatabase()
        startActivity<AlertListActivity>()
        this.finish()
    }
    private fun initDatabase()
    {
        val dbh = MeDbHelper(
                applicationContext)
        val sqlDB = dbh.writableDatabase
        sqlDB.close()
        dbh.close()
    }
    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private fun verifyPermissions() {
        if (!isGooglePlayServicesAvailable) {
            acquireGooglePlayServices()
        } else if (mCredential.selectedAccountName == null) {
            chooseAccount()
        } else if (!isDeviceOnline) {
            tv_Initializing.text = "No network connection available"
        } else {
            MakeRequestTask(mCredential).execute()
        }
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     * Google Play Services on this device.
     */
    internal fun showGooglePlayServicesAvailabilityErrorDialog(
            connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
                this@SplashActivity,
                connectionStatusCode,
                SplashActivity.REQUEST_GOOGLE_PLAY_SERVICES)
        dialog.show()
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    //@AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private fun chooseAccount() {
        if (EasyPermissions.hasPermissions(
                        this, Manifest.permission.GET_ACCOUNTS)) {
            val accountName = mPrefs.getString(SplashActivity.PREF_ACCOUNT_NAME, null)
            if (accountName != null) {
                mCredential.selectedAccountName = accountName
                verifyPermissions()
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        SplashActivity.REQUEST_ACCOUNT_PICKER)
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    SplashActivity.REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS)
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     * activity result.
     * @param data Intent (containing result data) returned by incoming
     * activity result.
     */
    override fun onActivityResult(
            requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SplashActivity.REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != Activity.RESULT_OK) {
                tv_Initializing.text = "Please install Google Play Services"
            } else {
                verifyPermissions()
            }
            SplashActivity.REQUEST_ACCOUNT_PICKER -> if (resultCode == Activity.RESULT_OK && data != null &&
                    data.extras != null) {
                val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (accountName != null) {
                    val editor = mPrefs.edit()
                    editor.putString(SplashActivity.PREF_ACCOUNT_NAME, accountName)
                    editor.apply()
                    mCredential.selectedAccountName = accountName
                    verifyPermissions()
                }
            }
            SplashActivity.REQUEST_AUTHORIZATION -> if (resultCode == Activity.RESULT_OK) {
                verifyPermissions()
            }
        }
    }

    /**
     * An asynchronous task that handles the Gmail API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private inner class MakeRequestTask internal constructor(credential: GoogleAccountCredential) : AsyncTask<Void, Void, Boolean>() {
        private var mService: com.google.api.services.gmail.Gmail? = null
        private var mLastError: Exception? = null

        /**
         * Fetch a list of Gmail labels attached to the specified account.
         * @return List of Strings labels.
         * @throws IOException
         */
        private// Get the labels in the user's account.
        val dataFromApi: Boolean
        // @Throws(IOException::class)
            get() {
                val user = "me"
                val listResponse = mService!!.users().messages().list(user).setQ("from:ebird-alert").execute()
                return listResponse != null
            }

        init {
            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory = JacksonFactory.getDefaultInstance()
            mService = com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("meAlerts")
                    .build()
        }

        /**
         * Background task to call Gmail API.
         * @param params no parameters needed for this task.
         */
        override fun doInBackground(vararg params: Void): Boolean? {
            try {
                return dataFromApi
            } catch (e: Exception) {
                mLastError = e
                cancel(true)
                return null
            }
        }

        override fun onPreExecute() {
            //mOutputText!!.text = ""
            //mProgress.show();
        }

        override fun onPostExecute(accessible: Boolean?) {
            //mProgress.hide();
            if (accessible == null || !accessible) {
                tv_Initializing.text =  "Account not accessible"
            } else {
                val editor = mPrefs.edit()
                editor.putBoolean(SplashActivity.PREF_PERMISSIONS_OK, true)
                editor.apply()
                startMainActivity()
            }
        }

        override fun onCancelled() {
            //mProgress.hide();
            if (mLastError != null) {
                if (mLastError is GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            (mLastError as GooglePlayServicesAvailabilityIOException)
                                    .connectionStatusCode)
                } else if (mLastError is UserRecoverableAuthIOException) {
                    startActivityForResult(
                            (mLastError as UserRecoverableAuthIOException).intent,
                            SplashActivity.REQUEST_AUTHORIZATION)
                } else {
                    tv_Initializing.text =  mLastError!!.message.toString()
                }
            } else {
                tv_Initializing.text = "Request Cancelled"
            }
        }
    }
}
