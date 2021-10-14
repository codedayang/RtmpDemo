package link.dayang.rtmpdemo.ble

import android.content.Context
import android.util.Log
import com.google.android.material.transition.Hold
import com.hc.bluetoothlibrary.DeviceModule
import com.jeremyliao.liveeventbus.LiveEventBus
import java.util.*

sealed class BleEvent
data class BleScanUpdateEvent(val deviceModule: DeviceModule) : BleEvent()
object BleScanEndEvent : BleEvent()
object BleScanFoundHcEvent : BleEvent()
object BleConnectPendingEvent : BleEvent()
object BleConnectSuccessEvent : BleEvent()
object BleConnectErrorEvent : BleEvent()
data class BleDataReadEvent<T>(val data: T) : BleEvent()
data class BleData(
        val beat: String,
        val oxy: String,
        val temp: String,
        val distance: String
)


object HcBleManager {

    private var mDeviceModule: DeviceModule? = null
    private var mConnected = false
    private var mConnecting = false

    private val mMessageBuffer = ArrayDeque<String>(100)

    private val eventListeners = mutableListOf<(BleEvent) -> Unit>()

    private val bleDataBuffer = mutableListOf<String>()

    fun init(context: Context) {
        HoldBluetooth.getInstance().initHoldBluetooth(context, object : HoldBluetooth.UpdateList {
            override fun update(isStart: Boolean, deviceModule: DeviceModule?) {
                if (deviceModule == null) {
//                    if (!isStart && !HcBleManager.isFound()) notifyEvent(BleScanEndEvent)
                    return
                }
                val name = deviceModule.name
                notifyEvent(BleScanUpdateEvent(deviceModule))
                if (name.contains(DEVICE_NAME_PATTERN)) {
                    notifyEvent(BleScanFoundHcEvent)
                    mDeviceModule = deviceModule
                }
            }

            override fun updateMessyCode(isStart: Boolean, deviceModule: DeviceModule?) {
                return update(isStart, deviceModule)
            }
        })

        HoldBluetooth.getInstance().setOnReadListener(object : HoldBluetooth.OnReadDataListener {
            override fun errorDisconnect(deviceModule: DeviceModule?) {
                mConnected = false
                mConnecting = false
                notifyEvent(BleConnectErrorEvent)
            }


            override fun readData(mac: String?, data: ByteArray?) {
                data?.let {
                    val str = decodeData(data)
//                    notifyEvent(BleDataReadEvent(str))
//                    mMessageBuffer.add(str)

                    val seqList = str.split("\r\n")
                    seqList.forEach {
                        val pieceList = it.split(" ")
                        pieceList.forEach PList@ { pis ->
                            val piece = pis.trim()
                            if (piece.isEmpty() || piece.isBlank()) {
                                return@PList
                            }
                            if (piece.toIntOrNull() != null && bleDataBuffer.isEmpty()) {
                                if (piece.toInt() >= 100) {
                                    bleDataBuffer.add(piece)
                                    return@PList
                                }
                            }
                            if (piece.toIntOrNull() != null && bleDataBuffer.size == 1) {
                                bleDataBuffer.add(piece)
                                return@PList
                            }
                            if (piece.toFloatOrNull() != null && bleDataBuffer.size == 2) {
                                if (piece.toFloat() <= 100) {
                                    bleDataBuffer.add(piece)
                                    return@PList
                                }
                            }
                            if (piece.toFloatOrNull() != null && bleDataBuffer.size == 3) {
                                bleDataBuffer.add(piece)
                                return@PList
                            }
                        }
                        if (bleDataBuffer.size >= 4) {
                            notifyEvent(BleDataReadEvent(BleData(
                                    bleDataBuffer[0],bleDataBuffer[1],
                                    bleDataBuffer[2],bleDataBuffer[3]
                            )))
                            bleDataBuffer.clear()
                        }
                    }
                }

            }

            override fun connectSucceed() {
                mConnected = true
                mConnecting = false
                notifyEvent(BleConnectSuccessEvent)
            }

            override fun readLog(className: String?, data: String?, lv: String?) {}
            override fun readVelocity(velocity: Int) {}
            override fun endScan() {
                mConnected = false
                mConnecting = false
                notifyEvent(BleScanEndEvent)
            }

            override fun readNumber(number: Int) {}
            override fun reading(isStart: Boolean) {}

        })
    }

    fun connect() {
        HoldBluetooth.getInstance().connect(mDeviceModule)
        mConnecting = true
    }

    fun startScan() {
        HoldBluetooth.getInstance().scan(true)
    }

    fun stopScan() {
        HoldBluetooth.getInstance().stopScan()
    }

    fun isFound() = mDeviceModule != null
    fun isConnecting() = mConnecting
    fun isConnected() = mConnected

    fun notifyEvent(event: BleEvent) {
        eventListeners.forEach {
            it.invoke(event)
        }
    }

    fun registerEventListener(listener: (BleEvent) -> Unit) {
        eventListeners.add(listener)
        if (isConnected()) {
            listener(BleConnectSuccessEvent)
            return
        }
        if (isConnecting()) {
            listener(BleConnectPendingEvent)
        }

    }

    fun unregisterEventListener(listener: (BleEvent) -> Unit) {
        eventListeners.remove(listener)
    }

    private fun decodeData(data: ByteArray): String {
        return Analysis.getByteToString(data, "gbk", false)
    }

    const val DEVICE_NAME_PATTERN = "HC-08"
}