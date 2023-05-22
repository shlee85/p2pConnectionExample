package com.example.p2pconnectionexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.p2pconnectionexample.databinding.ActivityMainBinding
import com.google.gson.Gson
import java.io.Serializable

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    var p2pClient : WifiDirect ?= null

    data class P2PList(val name: String, val address: String) : Serializable

    private fun permissionCheck() {
        if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Log.d(TAG, "Version is Android13 ( Tiramisu )")
                if (checkSelfPermission(Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.NEARBY_WIFI_DEVICES
                        ),
                        PERMISSIONS_REQUEST_CODE
                    )
                }
            } else {
                Log.d(TAG, "Version is Android 13 미만.")
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.ACCESS_NETWORK_STATE
                    ),
                    PERMISSIONS_REQUEST_CODE
                )
            }

            Log.d(TAG, "Permission 확인")
        }
    }

    var p2pList : ArrayList<P2PList> = ArrayList()
    var putDeviceInfo : ArrayList<P2PList> = ArrayList()


    data class P2PObject(val p2pCli:WifiDirect?) : Serializable
    var p2pObject: ArrayList<P2PObject?> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionCheck()

        binding.startActivity.setOnClickListener {
            Log.d(TAG, "go Sub Activity [${p2pList[0].name}]")
            putDeviceInfo.add(p2pList[0])

            val intent = Intent(this@MainActivity, SubActivity::class.java)
            Log.d(TAG, "push intent [${putDeviceInfo[0]}]")

            //intent.putExtra("testSubActivity", putDeviceInfo)
            val data = P2PObject(p2pClient)
            Log.d(TAG, "p2pClient = $p2pClient, data = $data")

            intent.putExtra("testSubActivity", data)
            Log.d(TAG, "111111111")
            startActivity(intent)
            Log.d(TAG, "Success!! intent")
        }


        /* p2p 객체 생성 */
        p2pClient = WifiDirect(this@MainActivity)
        Log.d(TAG, "p2pClient addr = $p2pClient")
        //p2pObject.add(p2pClient)
        p2pClient?.setListener(object: WifiDirect.OnListener {
            override fun onConnecting() {
                TODO("Not yet implemented")
            }

            override fun onConnected(ip: String?, reachable: Boolean, deviceName: String) {
                Log.d(TAG, "onConnected!!!")
            }

            override fun onWifiOff(str: String) {
                TODO("Not yet implemented")
            }

            override fun onDiscoverService(p2plist: ArrayList<WifiDirect.P2PList>) {
                Log.d(TAG, "onDiscoverService")
                p2plist.forEach {
                    Log.d(TAG, "${it.name} : ${it.address}")

                    /* WIFI가 로와시스 디바이스만 저장 */
                    if(it.name.contains("LOWASIS")) {
                        p2pList.add(P2PList(it.name, it.address))
                    }
                }
            }

            override fun onGroupInfo() {
                TODO("Not yet implemented")
            }
        })

        //p2p실행.
        binding.p2plist.setOnClickListener {
            Log.d(TAG, "P2p 리스트 조회")
            p2pClient?.p2pStart(false) //리스트 요청.
        }

        binding.p2pconnect.setOnClickListener {
            Log.d(TAG, "P2p 연결 (첫번째 들어온데이터 강제연결 - 테스트용도)")
            p2pClient?.p2pConnect(p2pList[0].name, p2pList[0].address)
        }
    }

    companion object {
        val TAG = MainActivity::class.java.simpleName
        const val PERMISSIONS_REQUEST_CODE = 1001
    }
}