package com.example.p2pconnectionexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.p2pconnectionexample.databinding.ActivityMainBinding
import java.io.Serializable

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    var p2pList : ArrayList<P2PList> = ArrayList()
    private var p2pData : ArrayList<String> = ArrayList()

    private var mDeviceName = ""

    private lateinit var adapter: P2pListAdapter
    //private var pList = arrayListOf<P2pDevice>()
    private var pList = ArrayList<P2pDevice>()

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

            override fun onDiscoverService(peers: ArrayList<P2pDevice>) {
                Log.d(TAG, "onDiscoverService")
//                pList.clear()
//                peers.forEach {
//
//                    Log.d(TAG, "pList Size = ${pList.size}")
//                    pList.add(P2pDevice(it.name, it.address))
//                }
//
//                p2pListRecyclerView()


                peerData(peers)
            }

            override fun onGroupInfo() {
                TODO("Not yet implemented")
            }

            override fun onDisconnected() {
                Log.d(TAG, "onDisconnected!!")
            }

            override fun onConnectFail() {
                Log.d(TAG, "onConnect Fail. [ $mDeviceName ], dialog = $dialog")
                showDialogMessage(DIALOG_CONNECTING_FAIL)
            }
        })

        //MainActivity에서는 무조건 peer검색요청 한다.
        WifiDirectSingleton.getInstance()?.p2pStart(false)

        p2pListRecyclerView()
    }

    private fun p2pListRecyclerView() {
        binding.p2plistRecyclerview?.layoutManager = LinearLayoutManager(AT3App.context)
        adapter = P2pListAdapter(AT3App.context!!, pList)
        binding.p2plistRecyclerview?.adapter = adapter

        adapter.setMyItemClickListener(object : P2pListAdapter.MyItemClickListener {
            override fun onItemClick(pos: Int, name: String?) {
                Log.d(TAG, "P2P 선택!![$pos : $name]")
                pList.forEach { p2p ->
                    if(p2p.name == name) {
                        mDeviceName = p2p.name
                        showDialogMessage(DIALOG_CONNECTING)
                        WifiDirectSingleton.getInstance()?.p2pConnect(p2p.name,p2p.address)
                    }
                }
            }
        })
    }

    private lateinit var builder: AlertDialog.Builder
    private lateinit var dialog: AlertDialog

    private fun showDialogMessage(code: Int) {
//        val builder = AlertDialog.Builder(this)
//        val dialog = builder.create()

        if(!::dialog.isInitialized) {
            builder = AlertDialog.Builder(this)
            dialog = builder.create()
            dialog.setTitle("Wi-Fi P2P")
        }

        val message = when(code) {
            DIALOG_CONNECTING -> {
                Log.d(TAG, "dialog 1 = $dialog")
                "P2P 연결 중입니다. 잠시만 기다리세요."
            }
            DIALOG_CONNECTING_FAIL -> {
                Log.d(TAG, "dialog 2 = $dialog")
                "P2P연결 실패 하였습니다. 다시 연결을 시도 하세요."
            }
            else -> ""
        }
        dialog.setMessage(message)
        dialog.show()
    }

    private fun moveSubActivity(ip: String) {
        val intent = Intent(this@MainActivity, SubActivity::class.java)
        intent.putExtra("subActivity", ip)
        startActivity(intent)
    }

    var idx = 0
    private fun peerData(peers: ArrayList<P2pDevice>) {
        //pList에 peers의 데이터가 없는 경우 추가.
        for( peer in peers ) {
            if(!pList.contains(peer)) {
                pList.add(peer)
                adapter.notifyItemChanged(idx)
            }
            idx++
        }

        idx = 0
        //pList에 데이터중에 peers에 없는 데이터는 제거
        val itr = pList.iterator()
        while(itr.hasNext()) {
            val p2p = itr.next()
            if(!peers.contains(p2p)) {
                itr.remove()
                adapter.notifyItemRemoved(idx)
            }
            idx++
        }

        pList.forEach {
            Log.d(TAG, "=> ${it.name}")
        }

        Log.d(TAG, "==============================")
    }

    companion object {
        val TAG = MainActivity::class.java.simpleName
        const val PERMISSIONS_REQUEST_CODE = 1001

        private const val DIALOG_CONNECTING = 1 //connecting.
        private const val DIALOG_CONNECTING_FAIL = 2 //connecting fail.
    }
}