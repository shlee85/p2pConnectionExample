package com.example.p2pconnectionexample

import android.app.Application
import android.content.Context
import android.util.Log

class AT3App : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate()")

        application = this
    }

    override fun onTerminate() {
        Log.d(TAG, "[onTerminate]")
        super.onTerminate()
    }

    companion object{
        private val TAG = AT3App::class.java.simpleName
        private var application: Application? = null
        val context: Context?
            get() {
                var context: Context? = null
                if (application != null) {
                    context = application?.applicationContext
                }
                return context
            }
    }
}