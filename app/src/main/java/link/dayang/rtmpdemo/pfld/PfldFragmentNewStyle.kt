package link.dayang.rtmpdemo.pfld

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.convertTo
import androidx.fragment.app.Fragment
import com.sample.tracking.FaceOverlapFragment
import link.dayang.rtmpdemo.R
import link.dayang.rtmpdemo.ble.*
import java.util.*

class PfldFragmentNewStyle : Fragment() {

    private lateinit var root: ViewGroup

    private lateinit var mBleTip: TextView
    private lateinit var mFatiTip: TextView
    private lateinit var mHwTip: TextView

    private val mBleListener: (BleEvent) -> Unit = {
        when (it) {
            BleConnectSuccessEvent -> {
                mBleTip.text = "蓝牙已连接"
                mHwTip.text = "您当前的硬件：已连接"
            }
            BleConnectPendingEvent -> {
                mBleTip.text = "正在连接蓝牙"
                mHwTip.text = "您当前的硬件：正在连接"
            }
            BleConnectErrorEvent -> {
                mBleTip.text = "请重试"
                mHwTip.text = "您当前的硬件：已断开"
            }
        }
    }

    private val socketListener = object : SocketListener {
        override fun onConnected() {
            mFatiTip.text = "已连接,正在检测"
        }

        override fun onResult(isFatigue: Boolean, latency: Long) {
            mFatiTip.text = if (isFatigue) "您现在已经处于疲劳状态"
                else "已连接,正在检测"
        }

        override fun onError() {
            mFatiTip.text = "网络连接出错, 请重启APP"
        }
    }

    private val socketKeeper = SocketKeeper(socketListener)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_pfld_new_style, container, false)
        root = view as ViewGroup
        initViews()
        HcBleManager.registerEventListener(mBleListener)

        initSocket()
        return view
    }

    private fun initSocket() {
        socketKeeper.connect()
    }

    private fun initViews() {
        val overlapFragment = FaceOverlapFragment()
        val tran = parentFragmentManager.beginTransaction()
        tran.replace(R.id.pfld_container, overlapFragment)
        tran.commit()

        mBleTip = root.findViewById<TextView>(R.id.enter_ble)
        mHwTip = root.findViewById(R.id.hw_tip)
        mFatiTip = root.findViewById(R.id.fati_tip)
        mBleTip.setOnClickListener {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            if (!adapter.isEnabled) {
                Toast.makeText(context, "请开启手机蓝牙", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (HcBleManager.isConnected()) {
                val intent = Intent(activity, HwActivity::class.java)
                startActivity(intent)
            } else {
                BleDialog().show(parentFragmentManager, null)
            }
        }


    }
}