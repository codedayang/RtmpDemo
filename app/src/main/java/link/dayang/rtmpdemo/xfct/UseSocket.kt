package link.dayang.rtmpdemo.xfct

import link.dayang.rtmpdemo.pfld.SocketKeeper
import link.dayang.rtmpdemo.pfld.SocketListener

class UseSocket(
    private val service: DetectorService,
    private val listener: SocketListener
) {

    lateinit var keeper: SocketKeeper

    fun send(P70: Float, maxMouth: Int) {
        keeper.send(P70, maxMouth)
    }

    fun create() {
        keeper = SocketKeeper(listener)

    }

    fun release() {


    }

    fun start() {
        keeper.connect()
    }

    fun stop() {
    }
}