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
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Timer
import kotlin.concurrent.timer

class SubActivity : AppCompatActivity() {
    lateinit var binding: ActivitySubBinding
    private var ipAddress: String? = ""

    private var pingCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "Sub Activity")
        val intent = intent
        val info = intent.getStringExtra("subActivity")
        ipAddress = info
        Log.d(TAG, "info = $ipAddress")

        if(ipAddress != "") pingTest()

        //객체를 전달 받아서 Disconnect 해보자.
        binding.p2pdisconnect.setOnClickListener {
            Log.d(TAG, "P2P Disconnect")
            releaseP2p()
        }

        WifiDirectSingleton.getInstance()?.setListener(object : WifiDirectSingleton.OnListener{
            override fun onConnecting() {
                TODO("Not yet implemented")
            }

            override fun onConnected(ip: String, reachable: Boolean, deviceName: String) {
                Log.d(TAG, "연결 완료!")
            }

            override fun onWifiOff(str: String) {
                TODO("Not yet implemented")
            }

            override fun onDiscoverService(p2plist: ArrayList<P2pDevice>) {
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

            override fun onConnectFail() {
                TODO("Not yet implemented")
            }
        })
    }

    private fun goMainActivity() {
        val intent = Intent(this@SubActivity, MainActivity::class.java)
        intent.putExtra("goMainActivity", "")
        startActivity(intent)
    }

    private fun releaseP2p() {
        testPingService?.cancel()
        WifiDirectSingleton.getInstance()?.p2pDisconnect()
    }

    private var testPingService: Timer? = null // 테스트용 핑
    private fun pingTest() {
        if (testPingService == null) { // 이 타이머는 서버와의 연결 확인 테스트 용임
            testPingService = timer(period = 1000, initialDelay = 1000)
            {
                try {
                    val address: InetAddress = InetAddress.getByName(ipAddress)
                    val reachable: Boolean = address.isReachable(1000)
                    val currentTime = SimpleDateFormat(
                        "hh:mm:ss",
                        Locale.getDefault()
                    ).format(Date(System.currentTimeMillis()))
                    val strText = "$currentTime  ping:$reachable"
                    runOnUiThread { binding.pingView.text = strText }
                    if(!reachable){
                        pingCount++
                    } else {
                        pingCount = 0
                    }

                    if(pingCount > 5) {
                        Log.d(TAG, "연결 끊김. 연결을 해제 합니다.")
                        releaseP2p()
                    }

                    Log.i(TAG, strText)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    companion object {
        val TAG: String = SubActivity::class.java.simpleName
    }
}