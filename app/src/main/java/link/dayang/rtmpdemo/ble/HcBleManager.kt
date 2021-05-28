package link.dayang.rtmpdemo.ble

import android.content.Context

object HcBleManager {
    fun init(context: Context) {
        HoldBluetooth.getInstance().initHoldBluetooth(context, null)
    }
}