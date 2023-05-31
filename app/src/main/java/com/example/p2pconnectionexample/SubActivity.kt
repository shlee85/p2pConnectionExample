package com.example.p2pconnectionexample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.p2pconnectionexample.databinding.ActivitySubBinding
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

            override fun onConnected(ip: String, reachable: Boolean, deviceName: String) {}

            override fun onWifiOff(str: String) {}

            override fun onDiscoverService(p2plist: ArrayList<P2pDevice>) {}

            override fun onGroupInfo() {}

            override fun onDisconnected() {
                Log.d(TAG, "onDisconnected!!")
                moveMainActivity()
                ActivityCompat.finishAffinity(this@SubActivity)
            }

            override fun onConnectFail() {}
        })
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume()")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy()")
    }

    private fun moveMainActivity() {
        val intent = Intent(this@SubActivity, MainActivity::class.java)
        intent.putExtra("goMainActivity", "")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        ActivityCompat.finishAffinity(this@SubActivity)
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
                    val strText = "$currentTime  ping:$reachable ( $ipAddress )"

                    runOnUiThread {
                        binding.pingView.text = strText
                        if(!reachable){
                            pingCount++
                        } else {
                            pingCount = 0
                        }

                        if(pingCount > 5) {
                            Log.d(TAG, "연결 끊김. 연결을 해제 합니다.")
                            Toast.makeText(this@SubActivity, "P2P 연결이 해제 되었습니다.", Toast.LENGTH_SHORT).show()
                            releaseP2p()
                        }
                    }

                    Log.i(TAG, strText)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            Log.d(TAG, "onKeyDown.")
            releaseP2p()
            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    companion object {
        val TAG: String = SubActivity::class.java.simpleName
    }
}