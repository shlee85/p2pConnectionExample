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

        //Log.d(TAG, "${intent.getStringExtra("testSubActivity")}")

        //객체를 전달 받아서 Disconnect 해보자.
        binding.p2pdisconnect.setOnClickListener {
            Log.d(TAG, "P2P Disconnect")
            WifiDirectSingleton.getInstance()?.p2pDisconnect()
        }

        WifiDirectSingleton.getInstance()?.setListener(object : WifiDirectSingleton.OnListener{
            override fun onConnecting() {
                TODO("Not yet implemented")
            }

            override fun onConnected(ip: String?, reachable: Boolean, deviceName: String) {
                Log.d(TAG, "연결 완료!")
            }

            override fun onWifiOff(str: String) {
                TODO("Not yet implemented")
            }

            override fun onDiscoverService(p2plist: ArrayList<WifiDirectSingleton.P2PList>) {
                Log.d(TAG, "onDiscoverService")
                p2plist.forEach {
                    Log.d(TAG, "${it.name} : ${it.address}")
                }
            }

            override fun onGroupInfo() {
                TODO("Not yet implemented")
            }

            override fun onDisconnected() {
                Log.d(TAG, "onDisconnected!!")
                goMainActivity()
            }
        })
    }

    private fun goMainActivity() {
        val intent = Intent(this@SubActivity, MainActivity::class.java)
        intent.putExtra("goMainActivity", "")
        startActivity(intent)
    }

    companion object {
        val TAG: String = SubActivity::class.java.simpleName
    }
}