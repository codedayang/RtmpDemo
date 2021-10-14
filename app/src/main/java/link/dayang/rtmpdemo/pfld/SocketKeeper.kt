package link.dayang.rtmpdemo.pfld

import android.util.Log
import com.google.gson.Gson
import link.dayang.rtmpdemo.data.UserModel
import link.dayang.rtmpdemo.util.RDExecutor
import java.io.*
import java.net.Socket
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class SocketKeeper(private val listener: SocketListener
) {
    private var socket: Socket? = null
    var isConnected = false

    private var socketIn: BufferedReader? = null
    private var socketOut: PrintWriter? = null

    private val pktMap = ConcurrentHashMap<String, FatigueFeatureItemReq>()


    private val readerThread = Thread {
        var content: String? = null
        try {
            while (true) {
                if (socket != null && socket!!.isConnected && !socket!!.isInputShutdown) {
                    content = socketIn?.readLine()
                    if ((content) != null) {
                        Log.v("dydy", content)
                        val res = Gson().fromJson<FatigueFeatureItemRes>(
                            content.trim(),
                            FatigueFeatureItemRes::class.java
                        )
//                        val sendTime = pktMap.remove(res.uuid)?.timestamp
//                        Log.v("dydy", sendTime.toString())
//                        Log.v("dydy", res.timestamp.toString())


//                        val latency = if (sendTime == null) {
//                            -1
//                        } else {
//                            res.timestamp-sendTime
//                        }
                        if (res.pred > 0.0F) {
                            RDExecutor.runOnMainThread { listener.onResult(true, 0) }
                        } else {
                            RDExecutor.runOnMainThread { listener.onResult(false, 0) }

                        }
                    }
                }
            }
        } catch (e: Exception) {
            isConnected = false
            RDExecutor.runOnMainThread { listener.onError() }
            e.printStackTrace()

        }
    }

    fun connect() {
        RDExecutor.runOnNetworkThread {
            try {
                socket = Socket(SERVER, PORT)
                isConnected = socket!!.isConnected
                socketIn = BufferedReader(InputStreamReader(socket!!.getInputStream(), "UTF-8"))
                socketOut = PrintWriter(
                    BufferedWriter(OutputStreamWriter(socket!!.getOutputStream())),
                    true
                )

                readerThread.start()
                RDExecutor.runOnMainThread { listener.onConnected() }
            } catch (e: Exception) {
                RDExecutor.runOnMainThread { listener.onError() }
                e.printStackTrace()
            }

        }
    }

    fun send(p70: Float, maxMouth: Int) {
        RDExecutor.runOnNetworkThread {
            try {
                if (socket != null && socket!!.isConnected && !socket!!.isOutputShutdown) {
                    val item = FatigueFeatureItemReq(
//                        UserModel.token,
//                        UUID.randomUUID().toString(),
                        p70,
                        maxMouth,
//                        System.currentTimeMillis()
                    )
                    Log.v("dydy", Gson().toJson(item))

//                    pktMap[item.uuid] = item

                    socketOut!!.print(Gson().toJson(item))
                    socketOut!!.println("over")
                }
            } catch (e: Exception) {
                RDExecutor.runOnMainThread { listener.onError() }
                e.printStackTrace()
            }

        }

    }


    companion object {
//        const val SERVER = "192.168.18.188"
//        const val SERVER = "server.natappfree.cc"
//        const val PORT = 43797

//        const val SERVER = "cn-bj-ali-2.natfrp.cloud"
//        const val PORT = 37857

        const val SERVER = "152.136.187.78"
        const val PORT = 55533
    }

}

interface SocketListener {
    fun onConnected()
    fun onResult(isFatigue: Boolean, latency: Long) // latency毫秒
    fun onError() // 连接错误或发送错误 自动断开并尝试重连

}