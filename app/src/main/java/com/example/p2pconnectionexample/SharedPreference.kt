package com.example.p2pconnectionexample

import android.content.Context
import android.content.SharedPreferences

object SharedPreference {
    private val TAG = SharedPreference::class.java.simpleName

    private const val KEY_LATEST = "latest_p2p_device"
    private const val PREF_FILE_NAME = "AT3SharedPref"

    private val sharedPreferences: SharedPreferences?
        get() {
            return AT3App.context?.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
        }

    private fun getString(key: String?, defValue: String): String {
        if (key == null) return defValue
        return sharedPreferences?.getString(key, defValue) ?: defValue
    }

    private fun putString(key: String?, value: String?) {
        if (key == null || value == null) return
        sharedPreferences?.edit()?.putString(key, value)?.apply()
    }

    var latest_p2p_device
        get() = getString(KEY_LATEST, "")
        set(value){
            putString(KEY_LATEST, value)
        }
}