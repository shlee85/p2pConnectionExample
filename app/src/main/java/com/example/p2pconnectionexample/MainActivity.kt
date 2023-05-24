package com.example.p2pconnectionexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.p2pconnectionexample.databinding.ActivityMainBinding
import com.google.gson.Gson
import java.io.Serializable

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    var p2pClient : WifiDirect ?= null

    var p2pList : ArrayList<P2PList> = ArrayList()
    var putDeviceInfo : ArrayList<P2PList> = ArrayList()


    private lateinit var adapter: P2pListAdapter
    private var pList = arrayListOf<P2pDevice>()

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionCheck()

    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart()!!")

        WifiDirectSingleton.setInstance(WifiDirectSingleton())
        WifiDirectSingleton.getInstance()?.setListener(object : WifiDirectSingleton.OnListener{
            override fun onConnecting() {
                TODO("Not yet implemented")
            }

            override fun onConnected(ip: String, reachable: Boolean, deviceName: String) {
                Log.d(TAG, "연결 완료! Go SubActivity!!, ip = $ip")
                moveSubActivity(ip)
            }

            override fun onWifiOff(str: String) {
                TODO("Not yet implemented")
            }

            override fun onDiscoverService(p2plist: ArrayList<WifiDirectSingleton.P2PList>) {
                Log.d(TAG, "onDiscoverService")
                pList.clear()
                p2plist.forEach {
                    Log.d(TAG, "${it.name} : ${it.address}")
                    p2pList.add(P2PList(it.name, it.address))
                }

                p2plist.distinct().forEach {
                    pList.add(P2pDevice(it.name, it.address))
                }

                p2pListRecyclerView(pList)
            }

            override fun onGroupInfo() {
                TODO("Not yet implemented")
            }

            override fun onDisconnected() {
                Log.d(TAG, "onDisconnected!!")
            }
        })

        //MainActivity에서는 무조건 peer검색요청 한다.
        WifiDirectSingleton.getInstance()?.p2pStart(false)
    }

    private fun p2pListRecyclerView(p2pList: ArrayList<P2pDevice>) {
        binding.p2plistRecyclerview.layoutManager = LinearLayoutManager(AT3App.context)
        adapter = P2pListAdapter(AT3App.context!!, pList)
        binding.p2plistRecyclerview.adapter = adapter

        adapter.setMyItemClickListener(object : P2pListAdapter.MyItemClickListener {
            override fun onItemClick(pos: Int, name: String?) {
                Log.d(WifiDirectSingleton.TAG, " P2P 선택!![$pos : $name]")
                p2pList.forEach { p2p ->
                    if(p2p.name == name) {
                        WifiDirectSingleton.getInstance()?.p2pConnect(p2p.name,p2p.address)
                    }
                }
            }
        })
    }

    private fun moveSubActivity(ip: String) {
        val intent = Intent(this@MainActivity, SubActivity::class.java)
        intent.putExtra("subActivity", ip)
        startActivity(intent)
    }

    companion object {
        val TAG = MainActivity::class.java.simpleName
        const val PERMISSIONS_REQUEST_CODE = 1001
    }
}