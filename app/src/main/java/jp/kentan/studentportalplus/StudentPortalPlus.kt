package jp.kentan.studentportalplus

import android.app.Application
import android.util.Log

class StudentPortalPlus : Application() {

    companion object {
        const val TAG = "StudentPortalPlusApp"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }
}