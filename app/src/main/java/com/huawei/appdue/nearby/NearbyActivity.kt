package com.huawei.appdue.nearby

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.RemoteException
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.huawei.appdue.R
import com.huawei.appdue.nearby.permission.PermissionHelper
import com.huawei.appdue.nearby.permission.PermissionInterface
import com.huawei.hms.nearby.Nearby
import com.huawei.hms.nearby.StatusCode
import com.huawei.hms.nearby.discovery.*
import com.huawei.hms.nearby.transfer.Data
import com.huawei.hms.nearby.transfer.DataCallback
import com.huawei.hms.nearby.transfer.TransferEngine
import com.huawei.hms.nearby.transfer.TransferStateUpdate
import java.nio.charset.Charset


class NearbyActivity: AppCompatActivity(), PermissionInterface, View.OnClickListener {

    private val TIMEOUT_MILLISECONDS = 10000
    private val TAG = "Nearby Connection Demo"

    private var mTransferEngine: TransferEngine? = null
    private var mDiscoveryEngine: DiscoveryEngine? = null

    private var mPermissionHelper: PermissionHelper? = null

    private var myNameEt: EditText? = null
    private var friendNameEt: EditText? = null
    private var msgEt: EditText? = null

    private var messageListView: ListView? = null

    private var msgList: List<MessageBean>? = null

    private var adapter: ChatAdapter? = null

    private var sendBtn: Button? = null
    private var connectBtn: Button? = null

    private var connectTaskResult = 0

    private var myNameStr: String? = null
    private var friendNameStr: String? = null
    private var myServiceId: String? = null
    private var mEndpointId: String? = null
    private var msgStr: String? = null

    val toastUtil: ToastUtil? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby)
        //requestPermissions()
        initView()
        sendBtn!!.isEnabled = false
        msgEt!!.isEnabled = false
    }

    private fun initView() {
        myNameEt = findViewById(R.id.et_my_name)
        friendNameEt = findViewById(R.id.et_friend_name)
        msgEt = findViewById(R.id.et_msg)
        connectBtn = findViewById(R.id.btn_connect)
        sendBtn = findViewById(R.id.btn_send)
        connectBtn!!.setOnClickListener(this)
        sendBtn!!.setOnClickListener(this)
        messageListView = findViewById(R.id.lv_chat)
        msgList = ArrayList()
        adapter = ChatAdapter(this, msgList)
        messageListView!!.setAdapter(adapter)
        connectTaskResult = StatusCode.STATUS_ENDPOINT_UNKNOWN
    }

    @SuppressLint("HandlerLeak")
    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            this.removeMessages(0)
            if (connectTaskResult != StatusCode.STATUS_SUCCESS) {
                //toastUtil!!.showShortToastTop("Connection timeout, make sure your friend is ready and try again.")
                if (myNameStr!!.compareTo(friendNameStr!!) > 0) {
                    mDiscoveryEngine!!.stopScan()
                } else {
                    mDiscoveryEngine!!.stopBroadcasting()
                }
                myNameEt!!.isEnabled = true
                friendNameEt!!.isEnabled = true
                connectBtn!!.isEnabled = true
            }
        }
    }

    private fun requestPermissions() {
        mPermissionHelper = PermissionHelper(this, this)
        mPermissionHelper!!.requestPermissions()
    }

    override fun getPermissionsRequestCode(): Int {
        return 10086
    }

    override fun getPermissions(): Array<String?>? {
        return arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun requestPermissionsSuccess() {}

    override fun requestPermissionsFail() {
        Toast.makeText(this, R.string.error_missing_permissions, Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>, grantResults: IntArray
    ) {
        if (mPermissionHelper!!.requestPermissionsResult(requestCode, permissions, grantResults)) {
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_connect -> {
                if (checkName()) {
                    return
                }
                connect(view)
                handler.sendEmptyMessageDelayed(0, TIMEOUT_MILLISECONDS.toLong())
            }
            R.id.btn_send -> {
                if (checkMessage()) {
                    return
                }
                sendMessage()
            }
            else -> {
            }
        }
    }

    private fun checkMessage(): Boolean {
        if (TextUtils.isEmpty(msgEt!!.text)) {
            toastUtil!!.showShortToastTop("Please input data you want to send.")
            return true
        }
        return false
    }

    private fun checkName(): Boolean {
        if (TextUtils.isEmpty(myNameEt!!.text)) {
            toastUtil!!.showShortToastTop("Please input your name.")
            return true
        }
        if (TextUtils.isEmpty(friendNameEt!!.text)) {
            toastUtil!!.showShortToastTop("Please input your friend's name.")
            return true
        }
        if (TextUtils.equals(
                myNameEt!!.text.toString(),
                friendNameEt!!.text.toString()
            )
        ) {
            toastUtil!!.showShortToastTop("Please input two different names.")
            return true
        }
        friendNameStr = friendNameEt!!.text.toString()
        myNameStr = myNameEt!!.text.toString()
        getServiceId()
        return false
    }

    private fun sendMessage() {
        msgStr = msgEt!!.text.toString()
        val data: Data = Data.fromBytes(msgStr!!.toByteArray(Charset.defaultCharset()))
        Log.d(TAG, "myEndpointId $mEndpointId")
        mTransferEngine!!.sendData(mEndpointId, data)
        val item = MessageBean()
        item.myName = myNameStr
        item.friendName = friendNameStr
        item.msg = msgStr
        item.isSend = true
        msgList!!.toMutableList().add(item)
        adapter!!.notifyDataSetChanged()
        msgEt!!.setText("")
        messageListView!!.setSelection(messageListView!!.bottom)
    }

    private fun receiveMessage(data: Data) {
        msgStr = String(data.asBytes())
        val item = MessageBean()
        item.myName = myNameStr
        item.friendName = friendNameStr
        item.msg = msgStr
        item.isSend = false
        msgList!!.toMutableList().add(item)
        adapter!!.notifyDataSetChanged()
        messageListView!!.setSelection(messageListView!!.bottom)
    }

    private fun connect(view: View) {
        //toastUtil!!.showShortToastTop("Connecting to your friend.")
        connectBtn!!.isEnabled = false
        myNameEt!!.isEnabled = false
        friendNameEt!!.isEnabled = false
        val context: Context = applicationContext
        mDiscoveryEngine = Nearby.getDiscoveryEngine(context)
        try {
            if (myNameStr!!.compareTo(friendNameStr!!) > 0) {
                doStartScan(view)
            } else {
                doStartBroadcast(view)
            }
        } catch (e: RemoteException) {
            Log.e(TAG, "remote exception." + e.message)
        }
    }

    @Throws(RemoteException::class)
    fun doStartBroadcast(view: View?) {
        val advBuilder = BroadcastOption.Builder()
        advBuilder.setPolicy(Policy.POLICY_STAR)
        mDiscoveryEngine!!.startBroadcasting(myNameStr, myServiceId, mConnCb, advBuilder.build())
    }

    private fun getServiceId() {
        myServiceId = if (myNameStr!!.compareTo(friendNameStr!!) > 0) {
            myNameStr + friendNameStr
        } else {
            friendNameStr + myNameStr
        }
    }

    @Throws(RemoteException::class)
    fun doStartScan(view: View?) {
        val discBuilder = ScanOption.Builder()
        discBuilder.setPolicy(Policy.POLICY_STAR)
        mDiscoveryEngine!!.startScan(myServiceId, mDiscCb, discBuilder.build())
    }

    private val mConnCb: ConnectCallback = object : ConnectCallback() {
        override fun onEstablish(endpointId: String, connectionInfo: ConnectInfo?) {
            mTransferEngine = Nearby.getTransferEngine(applicationContext)
            mEndpointId = endpointId
            mDiscoveryEngine!!.acceptConnect(endpointId, mDataCb)
            toastUtil!!.showShortToastTop("Let's chat!")
            sendBtn!!.isEnabled = true
            msgEt!!.isEnabled = true
            connectBtn!!.isEnabled = false
            connectTaskResult = StatusCode.STATUS_SUCCESS
            if (myNameStr!!.compareTo(friendNameStr!!) > 0) {
                mDiscoveryEngine!!.stopScan()
            } else {
                mDiscoveryEngine!!.stopBroadcasting()
            }
        }

        override fun onResult(endpointId: String, resolution: ConnectResult?) {
            mEndpointId = endpointId
        }

        override fun onDisconnected(endpointId: String?) {
            toastUtil!!.showShortToastTop("Disconnect.")
            connectTaskResult = StatusCode.STATUS_NOT_CONNECTED
            sendBtn!!.isEnabled = false
            connectBtn!!.isEnabled = true
            msgEt!!.isEnabled = false
            myNameEt!!.isEnabled = true
            friendNameEt!!.isEnabled = true
        }
    }

    private val mDiscCb: ScanEndpointCallback = object : ScanEndpointCallback() {
        override fun onFound(
            endpointId: String,
            discoveryEndpointInfo: ScanEndpointInfo?
        ) {
            mEndpointId = endpointId
            mDiscoveryEngine!!.requestConnect(myNameStr, mEndpointId, mConnCb)
        }

        override fun onLost(endpointId: String) {
            Log.d(TAG, "Nearby Connection Demo app: Lost endpoint: $endpointId")
        }
    }

    private val mDataCb: DataCallback = object : DataCallback() {
        override fun onReceived(
            string: String?,
            data: Data?
        ) {
            receiveMessage(data!!)
        }

        override fun onTransferUpdate(
            string: String?,
            update: TransferStateUpdate?
        ) {
        }
    }


}