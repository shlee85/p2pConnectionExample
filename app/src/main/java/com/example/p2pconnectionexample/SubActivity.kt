package com.example.p2pconnectionexample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.p2pconnectionexample.databinding.ActivitySubBinding
import com.google.gson.Gson
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.io.Serializable

class SubActivity : AppCompatActivity() {
    lateinit var binding: ActivitySubBinding

    var p2pClientTemp : WifiDirect? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "Sub Activity")
        val intent = intent
        val info = intent.getSerializableExtra("testSubActivity")
        Log.d(TAG, "info -> ${info}, p2pClientTemp = $p2pClientTemp")
        if(p2pClientTemp == info) {
            Log.d(TAG, "같다!!!!!")
        }
        //Log.d(TAG, "${intent.getStringExtra("testSubActivity")}")

        //객체를 전달 받아서 Disconnect 해보자.
        binding.p2pdisconnect.setOnClickListener {
            Log.d(TAG, "P2P Disconnect")
        }
    }

    companion object {
        val TAG: String = SubActivity::class.java.simpleName
    }
}