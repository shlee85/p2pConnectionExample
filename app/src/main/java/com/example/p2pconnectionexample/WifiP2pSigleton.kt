package com.example.p2pconnectionexample

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WifiDirectSingleton() :
    ConnectionInfoListener, Thread() {
    companion object {
        val TAG = WifiDirectSingleton::class.java.simpleName

        const val NETWORK_PASS_PHRASE = "SLy!x*8E"

        const val P2P_HANDLER_MSG_CONNECT = 0
        const val P2P_HANDLER_MSG_GROUP_INFO = 1
        const val P2P_HANDLER_MSG_DISCOVER_PEERS = 2
        const val P2P_HANDLER_MSG_STATE_CONNECTING = 3 // 상태 메시지 연결중
        const val P2P_HANDLER_MSG_STATE_WIFI_OFF = 4 // 상태 메시지 WIFI OFF
        const val P2P_HANDLER_MSG_STATE_CONNECTED = 5 // 상태 메시지 연결됨
        const val P2P_HANDLER_MSG_STATE_DISCONNECT = 6 //연결 종료 하기
        const val P2P_HANDLER_MSG_STATE_DISCONNECTED = 7 // 연결종료완료

        private var instance: WifiDirectSingleton? = null

        @Synchronized
        fun getInstance(): WifiDirectSingleton? {
            return instance
        }

        @Synchronized
        fun setInstance(value: WifiDirectSingleton?) {
            instance = value
        }
    }

    private var p2pHandler: Handler? = null

    private var p2pBroadcastReceiver: BroadcastReceiver? = null
    private var p2pServiceRequest: WifiP2pDnsSdServiceRequest? = null

    private var p2pChannel: WifiP2pManager.Channel?
    private var p2pManager: WifiP2pManager? = null

    private var mDeviceName: String = ""
    private var mDeviceAddress: String = ""

    data class P2PList(val name: String, val address: String)
    private var p2plist: ArrayList<P2PList> = ArrayList()

    init {
        p2pManager = AT3App.context?.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager?
        p2pChannel = p2pManager?.initialize(AT3App.context, AT3App.context?.mainLooper, null)
    }

    fun p2pStart(isStart: Boolean) {
        Log.d(TAG, "[WIFI-P2P] p2pStart(), p2pHandler = [$p2pHandler]")
        Log.d(TAG, "App Context = ${AT3App.context}")

        if(p2pHandler == null) {
            p2pHandler = Handler(Looper.getMainLooper()) { msg ->
                val strMessage = when (msg.what) {
                    P2P_HANDLER_MSG_STATE_CONNECTING -> "P2P_HANDLER_MSG_STATE_CONNECTING"
                    P2P_HANDLER_MSG_CONNECT -> "P2P_HANDLER_MSG_CONNECT"
                    P2P_HANDLER_MSG_STATE_CONNECTED -> "P2P_HANDLER_MSG_STATE_CONNECTED"
                    P2P_HANDLER_MSG_STATE_DISCONNECT -> "P2P_HANDLER_MSG_STATE_DISCONNECT"
                    P2P_HANDLER_MSG_STATE_DISCONNECTED -> "P2P_HANDLER_MSG_STATE_DISCONNECTED"
                    P2P_HANDLER_MSG_GROUP_INFO -> "P2P_HANDLER_MSG_GROUP_INFO"
                    P2P_HANDLER_MSG_DISCOVER_PEERS -> "P2P_HANDLER_MSG_DISCOVER_PEERS"
                    P2P_HANDLER_MSG_STATE_WIFI_OFF -> "P2P_HANDLER_MSG_STATE_WIFI_OFF"
                    else -> "UNKNOWN"
                }

                Log.i(TAG, "strMessage : $strMessage")

                when(msg.what) {
                    P2P_HANDLER_MSG_STATE_CONNECTING ->{
                        Log.d(TAG, "P2P_HANDLER_MSG_STATE_CONNECTING")
                    }
                    P2P_HANDLER_MSG_CONNECT -> {
                        Log.d(TAG, "P2P_HANDLER_MSG_CONNECT")
                        p2pConnectExec()
                    }
                    P2P_HANDLER_MSG_STATE_CONNECTED ->{
                        Log.d(TAG, "P2P_HANDLER_MSG_STATE_CONNECTED")
                        Log.d(TAG, "msgObj -> ${msg.obj} ")

                        stopDiscoveryPeer()
                        mListener?.onConnected(null, true, "null")
                    }
                    P2P_HANDLER_MSG_STATE_DISCONNECT -> {
                        Log.d(TAG, "P2P_HANDLER_MSG_STATE_DISCONNECT")
                        p2pDisconnectExec()
                    }
                    P2P_HANDLER_MSG_STATE_DISCONNECTED -> {
                        Log.d(TAG, "P2P_HANDLER_MSG_STATE_DISCONNECTED")

                        stopDiscoveryPeer()
                        mListener?.onDisconnected()
                    }
                    P2P_HANDLER_MSG_GROUP_INFO -> {
                        Log.d(TAG, "P2P_HANDLER_MSG_GROUP_INFO")
                        p2pGroupInfo()
                    }
                    P2P_HANDLER_MSG_DISCOVER_PEERS -> {
                        Log.d(TAG, "P2P_HANDLER_MSG_DISCOVER_PEERS")
                        discoveryPeer()
                    }
                    P2P_HANDLER_MSG_STATE_WIFI_OFF -> {
                        Log.d(TAG, "P2P_HANDLER_MSG_STATE_WIFI_OFF")
                    }
                    else -> {
                        Log.d(TAG, "UnKnown")
                    }
                }
                true
            }
        }

        if(isStart) {
            Log.d(TAG, "isStart is true")
        } else {
            Log.d(TAG, "p2p 리스트 요청.")
            p2pHandler?.sendEmptyMessage(P2P_HANDLER_MSG_DISCOVER_PEERS)
        }

        p2pBroadcastReceiver = object : BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(TAG, "BroadcastReceiver() onReceive")

                when(intent?.action) {
                    WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                        Log.d(TAG, "WIFI_P2P_STATE_CHANGED_ACTION")
                    }
                    WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                        Log.d(TAG, "WIFI_P2P_PEERS_CHANGED_ACTION")
                        p2pManager?.requestPeers(p2pChannel, peerListListener)
                    }
                    WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                        Log.d(TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION")
                    }
                    WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                        Log.i(TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION")
                    }
                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        AT3App.context?.registerReceiver(p2pBroadcastReceiver, intentFilter)
    }

    private fun discoveryPeer() {
        Log.d(TAG, "P2P 리스트 요청합니다.")
        p2pManager?.discoverPeers(p2pChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "P2p 리스트 요청 성공")
            }

            override fun onFailure(p0: Int) {
                Log.d(TAG, "P2p 리스트 요청 실패 : ${errorText(p0)}")
            }
        })
    }

    /*
     피어 검색 중단
     1. 연결 성공 후
     2. 연결 종료 후
    */
    private fun stopDiscoveryPeer() {
        Log.d(TAG, "Peer검색 중단을 요청 합니다.")
        p2pManager?.stopPeerDiscovery(p2pChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Peer검색 중단 요청이 성공")
            }

            override fun onFailure(p0: Int) {
                Log.d(TAG, "Peer검색 중단 요청이 실패 ( ${errorText(p0)} )")
            }
        })
    }

    private val peerListListener = WifiP2pManager.PeerListListener { peerList ->
        p2plist.clear()   //peerList가 호출 될 때 기존에 쌓은 데이터는 제거 하고 다시 처음부터 쌓는다.

        peerList.deviceList.forEach {
            Log.d(TAG, "List : ${it.deviceName}, ${it.deviceAddress}")
            if(it.deviceName.contains("LOWASIS")) {
                p2plist.add(P2PList(it.deviceName, it.deviceAddress))
            }
        }
        Log.d(TAG, "......................")
        mListener?.onDiscoverService(p2plist)
    }

    fun p2pConnect(name: String, address: String) {
        Log.d(TAG, "연결할 이름 : $name, 주소 : $address")
        mDeviceName = name
        mDeviceAddress = address
        p2pHandler?.sendEmptyMessage(P2P_HANDLER_MSG_CONNECT)
    }

    fun p2pConnectExec() {
        val config = WifiP2pConfig.Builder()
            .setNetworkName(mDeviceName)
            .setPassphrase(NETWORK_PASS_PHRASE)
            .enablePersistentMode(true)
            .setGroupOperatingBand(WifiP2pConfig.GROUP_OWNER_BAND_5GHZ)
            .build()
        config.deviceAddress = mDeviceAddress

        if(config.deviceAddress.isNotEmpty()) {
            if (ActivityCompat.checkSelfPermission(
                    AT3App.context!!,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    AT3App.context!!,
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.d(TAG, "Permission???")
                return
            }
            p2pManager?.connect(p2pChannel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d(TAG, "P2P 연결 성공!")
                    p2pHandler?.sendEmptyMessageDelayed(P2P_HANDLER_MSG_GROUP_INFO, 300)
                }

                override fun onFailure(p0: Int) {
                    Log.d(TAG, "P2P 연결 실패!")
                }
            })
        }
    }

    fun p2pDisconnect() {
        Log.d(TAG, "p2p Disconnect ")
        p2pHandler?.sendEmptyMessage(P2P_HANDLER_MSG_STATE_DISCONNECT)
    }

    private fun p2pDisconnectExec() {
        Log.d(TAG, "p2p Disconnect Exec! ")
        p2pManager?.removeGroup(p2pChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "P2P 연결 해제 성공!!!")
                p2pManager?.cancelConnect(p2pChannel, null)
                p2pManager?.clearLocalServices(p2pChannel, null)
                p2pManager?.clearServiceRequests(p2pChannel, null)

                p2pHandler?.sendEmptyMessage(P2P_HANDLER_MSG_STATE_DISCONNECTED)
            }

            override fun onFailure(p0: Int) {
                Log.d(TAG, "P2P 연결 해제 실패!!!")
            }

        })
    }

    private fun p2pGroupInfo() {
        if (ActivityCompat.checkSelfPermission(
                AT3App.context!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                AT3App.context!!,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "P2P GroupInfo permission!!!!")
            return
        }
        p2pManager?.requestGroupInfo(p2pChannel) { group ->
            if(group?.networkName == mDeviceName) {
                p2pManager?.requestConnectionInfo(p2pChannel) { info ->
                    if(info?.groupOwnerAddress?.hostAddress?.isNotBlank() == true) {
                        //stop
                        Message().let {
                            it.what = P2P_HANDLER_MSG_STATE_CONNECTED
                            it.obj = info.groupOwnerAddress?.hostAddress.toString()
                            p2pHandler?.sendMessage(it)
                        }
                    } else {
                        Log.d(TAG, "")
                        p2pHandler?.sendEmptyMessageDelayed(P2P_HANDLER_MSG_GROUP_INFO, 300)
                    }
                }
            } else {
                Log.d(TAG, "")
                p2pHandler?.sendEmptyMessageDelayed(P2P_HANDLER_MSG_GROUP_INFO, 300)
            }
        }
    }


    override fun onConnectionInfoAvailable(p0: WifiP2pInfo?) {
        Log.d(TAG, "onConnectionInfoAvailable()")
    }

    private var mListener: WifiDirectSingleton.OnListener?= null
    interface OnListener {
        fun onConnecting()
        fun onConnected(ip: String?, reachable: Boolean, deviceName: String)
        fun onWifiOff(str: String)
        fun onDiscoverService(p2plist: ArrayList<P2PList>)
        fun onGroupInfo()
        fun onDisconnected()
    }

    fun setListener(listener: OnListener) {
        mListener = listener
    }

    private fun errorText(error: Int) : String {
        val errorStr = when(error) {
            0 -> "Internal Error"
            1 -> "Unsupported"
            2 -> "BUSY. 프레임워크가 사용중. 요청 처리 불가."
            else -> "알수없음."
        }
        return errorStr
    }
}