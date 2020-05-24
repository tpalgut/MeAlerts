package com.olivejar.mealerts.data

import android.app.IntentService
import android.content.Intent
import android.content.Context
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import com.google.api.services.gmail.model.Message
import com.olivejar.mealerts.SplashActivity
import java.util.*
import com.google.api.client.googleapis.batch.BatchRequest
import com.google.api.client.googleapis.json.GoogleJsonError
import com.google.api.client.googleapis.batch.json.JsonBatchCallback
import com.google.api.client.http.HttpHeaders
import android.util.Base64
import com.google.api.client.util.DateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter


// IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
private const val ACTION_UPDATE_ALERTS = "com.olivejar.mealerts.data.action.UPDATE"
private const val ACTION_CLEAR_ALERTS = "com.olivejar.mealerts.data.action.CLEAR"

// TODO: Rename parameters
private const val EXTRA_PARAM1 = "com.olivejar.mealerts.data.extra.PARAM1"
private const val EXTRA_PARAM2 = "com.olivejar.mealerts.data.extra.PARAM2"

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * helper methods.
 */
class AlertIntentService : IntentService("AlertIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_UPDATE_ALERTS -> {
                handleActionUpdateAlerts()
            }
            ACTION_CLEAR_ALERTS -> {
                handleActionClearAlerts()
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionUpdateAlerts() {
        val accountName = getSharedPreferences(SplashActivity.PREFS_FILE,0)
                .getString(SplashActivity.PREF_ACCOUNT_NAME, "")
        val credential = GoogleAccountCredential.usingOAuth2(
        applicationContext, Arrays.asList(*SplashActivity.SCOPES))
        .setBackOff(ExponentialBackOff())
                .setSelectedAccountName(accountName)
        val transport = AndroidHttp.newCompatibleTransport()
        val jsonFactory = JacksonFactory.getDefaultInstance()
        val mService = com.google.api.services.gmail.Gmail.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("meAlerts")
                .build()
        val user = "me"
        loadAlerts(mService, user)

        val completedIntent = Intent()
        completedIntent.action = ALERTS_UPDATED
        sendBroadcast(completedIntent)
    }

    private fun loadAlerts(mService: Gmail?, userId: String) {
        val query = "from:ebird-alert"
        var response = mService!!.users().messages().list(userId).setQ(query).execute()
        val msgList = ArrayList<Message>()
        while (response.getMessages() != null) {
            msgList.addAll(response.getMessages())
            if (response.getNextPageToken() != null) {
                val pageToken = response.getNextPageToken()
                response = mService.users().messages().list(userId).setQ(query)
                        .setPageToken(pageToken).execute()
            } else {
                break
            }
        }

        val fullMessages = ArrayList<Message>()
        val callback = object : JsonBatchCallback<Message>() {
            override fun onSuccess(message: Message, responseHeaders: HttpHeaders) {
                fullMessages.add(message)
            }

            override fun onFailure(e: GoogleJsonError, responseHeaders: HttpHeaders) {
                // do what you want if error occurs
            }
        }
        val batch = mService.batch()
        for (message in msgList) {
            mService.users().messages().get(userId, message.getId()).setFormat("full").queue(batch, callback)
        }

        batch.execute()
        val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)
        //val date = LocalDate.parse(string, formatter)

        for (m in fullMessages){
            val msg = mService.users().messages().get(userId, m.id).execute();
            var body = String(Base64.decode(msg.payload.body.data,Base64.URL_SAFE))
            var count = 0
            var date = ""
            var subject = ""
            for(h in msg.payload.headers){
                if(h.name=="Date"){
                    count = count +1
                    date = h.value
                }
                else
                {
                    if (h.name=="Subject"){
                        count= count + 1
                        subject= h.value
                    }
                }
                if (count > 1){
                    break;
                }
            }

        }
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionClearAlerts() {
        TODO("Handle action Baz")
    }

    companion object {
        const val ALERTS_UPDATED = "OJS.Alerts.Updates"
        const val ALERTS_CLEARED = "OJS.Alerts.Cleared"
        private val SCOPES = arrayOf(GmailScopes.GMAIL_READONLY)
        /**
         * Starts this service to perform action Foo with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        @JvmStatic
        fun updateAlerts(context: Context) {
            val intent = Intent(context, AlertIntentService::class.java).apply {
                action = ACTION_UPDATE_ALERTS
            }
            context.startService(intent)
        }

        /**
         * Starts this service to perform action Baz with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        @JvmStatic
        fun ClearAlerts(context: Context) {
            val intent = Intent(context, AlertIntentService::class.java).apply {
                action = ACTION_CLEAR_ALERTS
            }
            context.startService(intent)
        }
    }
}
