package link.dayang.rtmpdemo.xfct

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.TextView
import androidx.annotation.NonNull
import link.dayang.rtmpdemo.R
import link.dayang.rtmpdemo.pfld.SocketListener
import link.dayang.rtmpdemo.util.dip2px
import java.util.*

class PfManager(
    private val service: DetectorService
) {

    private lateinit var viewRoot: ViewGroup
    private var windowManager: WindowManager? = null
    private var windowsLayoutParams: WindowManager.LayoutParams? = null

    private var cameraInited: Boolean = false
    private var cameraSurface: SurfaceView? = null
    private var cameraSurfaceHolder: SurfaceHolder? = null

    private val PREVIEW_WIDTH = 640
    private val PREVIEW_HEIGHT = 480

//    private lateinit var textureView: TextureView

    private lateinit var surfaceView: SurfaceView
    private lateinit var overlapView: SurfaceView
//    private lateinit var useCamera: UseCamera

    private lateinit var useCamera1: UseCamera1
    private lateinit var usePfld: UsePfld
    private lateinit var useFeatureCompute: UseFeatureCompute
    private lateinit var useSocket: UseSocket

    private lateinit var statusText: TextView

    private val onFeatureAvailable: (P70: Float, maxMouth: Int) -> Unit = {P70, maxMouth ->
        if (useSocket.keeper.isConnected) {
            useSocket.send(P70, maxMouth)
        }
    }


    fun init() {
        windowManager = service.getSystemService(Service.WINDOW_SERVICE) as WindowManager
        windowsLayoutParams = buildLayoutParams()

        val inflater = service.getSystemService(Service.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        viewRoot = inflater.inflate(R.layout.window_pfld, null) as ViewGroup
        (viewRoot as View).setOnTouchListener(FloatingOnTouchListener())

        statusText = viewRoot.findViewById(R.id.status_text)


//        textureView = viewRoot.findViewById(R.id.window_surface_camera)
        surfaceView = viewRoot.findViewById(R.id.window_surface_camera)
        overlapView = viewRoot.findViewById(R.id.windows_surface_overlap)

//        useCamera = UseCamera(service, textureView)
//        useCamera.create()

        useCamera1 = UseCamera1(service, surfaceView)
        useCamera1.create()

        usePfld = UsePfld(service, useCamera1, overlapView)
        usePfld.create()

        useFeatureCompute = UseFeatureCompute(service, usePfld, onFeatureAvailable)
        useFeatureCompute.create()

        useSocket = UseSocket(service, PfSocketListener())
        useSocket.create()


    }


    fun release() {

    }

    fun showWindow() {
        windowManager!!.addView(viewRoot, windowsLayoutParams)
        useCamera1.start()
        usePfld.start()
        useFeatureCompute.start()
        statusText.text = "正在连接服务器"
        useSocket.start()

    }

    fun hideWindow() {
        windowManager!!.removeView(viewRoot)
        useCamera1.stop()
        usePfld.stop()
        useFeatureCompute.stop()
        statusText.text = "正在连接服务器"
        useSocket.stop()
    }


    private fun buildLayoutParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }

            flags = (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            format = PixelFormat.TRANSLUCENT
            format = PixelFormat.RGBA_8888
//            width = service.dip2px(160F).toInt()
            width = 1080
//            height = service.dip2px(160F).toInt()
            height = 1920
//            gravity
            x = 0
            y = 0
        }
    }

    private inner class PfSocketListener : SocketListener {
        override fun onConnected() {
            statusText.text = "Standing by"
        }

        override fun onResult(isFatigue: Boolean, latency: Long) {
            if (isFatigue) {
                statusText.text = "您已疲劳"
                val uri = Uri.parse("android.resource://link.dayang.rtmpdemo/" + R.raw.bell_short);

                val tone = RingtoneManager.getRingtone(service, uri)
                tone.play()
            } else {
                statusText.text = "Standing by"
            }

        }

        override fun onError() {
//            statusText.text = "网络连接出错, 请重启APP"
        }

    }
    private inner class FloatingOnTouchListener : View.OnTouchListener {
        private var x = 0F
        private var y = 0F
        override fun onTouch(view: View?, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = event.rawX
                    y = event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    val nowX = event.rawX
                    val nowY = event.rawY
                    val movedX = nowX - x
                    val movedY = nowY - y
                    x = nowX
                    y = nowY
                    windowsLayoutParams!!.x = (windowsLayoutParams!!.x + movedX).toInt()
                    windowsLayoutParams!!.y = (windowsLayoutParams!!.y + movedY).toInt()

                    // 更新悬浮窗控件布局
                    windowManager!!.updateViewLayout(view, windowsLayoutParams!!)
                }
                else -> {
                }
            }
            return false
        }
    }

}