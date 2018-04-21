package jp.kentan.studentportalplus.notification

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.firebase.jobdispatcher.Constraint
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import com.firebase.jobdispatcher.Trigger

class RetryActionService : Service() {

    private companion object {
        const val TAG = "RetryActionService"
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")

        val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(this))

        val bundle = Bundle()
        bundle.putBoolean("ignore_midnight", true)

        val job = dispatcher.newJobBuilder()
                .setService(SyncJobService::class.java)
                .setExtras(bundle)
                .setTag(TAG)
                .setTrigger(Trigger.NOW)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .build()

        dispatcher.schedule(job)

        return super.onStartCommand(intent, flags, startId)
    }
}
