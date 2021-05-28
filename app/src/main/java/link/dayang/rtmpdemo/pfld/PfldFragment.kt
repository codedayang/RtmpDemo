package link.dayang.rtmpdemo.pfld

import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import com.google.gson.Gson
import com.sample.tracking.FaceOverlapFragment
import link.dayang.rtmpdemo.R
import link.dayang.rtmpdemo.data.UserModel
import link.dayang.rtmpdemo.util.JWTUtils
import java.lang.StringBuilder
import java.util.*
import kotlin.system.exitProcess

class PfldFragment : Fragment() {

    var counter = HitCounter()

    private lateinit var featureCalc: FeatureCalc

    private lateinit var socketKeeper: SocketKeeper

    private lateinit var fatigueBanner: ViewGroup

    private lateinit var statusTextView : TextView

    private lateinit var msg: TextView

    private lateinit var userid: TextView

    private var lastUpdate = 0L


    private val listener = object : SocketListener {
        override fun onConnected() {
            toast("服务器已连接")
            statusTextView.text = "已连接"
            statusTextView.setTextColor(Color.GREEN)
        }

        override fun onResult(isFatigue: Boolean, latency: Long) {
            setFatigueMsg(isFatigue)
            if (lastUpdate == 0L) {
                lastUpdate = Date().time
                statusTextView.text = "已连接"
            } else {
                val curTime = Date().time
                val updateInv = curTime - lastUpdate

                statusTextView.text = "已连接"
//                statusTextView.text = "已连接 更新于 $updateInv ms前"
                lastUpdate = curTime
            }
//            setFatigueBannerVisible(isFatigue)
//            toast("延迟 $latency")
            statusTextView.setTextColor(Color.GREEN)
        }

        override fun onError() {
//            toast("网络连接出错，请重启APP")
            statusTextView.text = "网络连接出错, 请重启APP"
            statusTextView.setTextColor(Color.RED)
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_pfld, container, false)
//        fatigueBanner = root.findViewById(R.id.fatigueBanner)

        userid = root.findViewById(R.id.userid)
//        userid.text = "用户id: ${JWTUtils.getUserSub(UserModel.token)}"
        userid.text = "用户id: 12"
        msg = root.findViewById(R.id.msg)
        statusTextView = root.findViewById(R.id.status)

        val overlapFragment = FaceOverlapFragment()
        val tran = parentFragmentManager.beginTransaction()
        tran.replace(R.id.pfld_container, overlapFragment)
        tran.commit()


        val textView = root.findViewById<TextView>(R.id.curr)

        featureCalc = FeatureCalc { P70, maxMouth ->
            val builder = StringBuilder()
            builder.append(P70)
            builder.append("\n")
            builder.append(maxMouth.toFloat() / 20F * 30F)
            textView.text = builder.toString()

            socketKeeper.send(P70, maxMouth)
        }

        overlapFragment.registTrackCallback {
            counter.count()
            if (it.isEmpty()) return@registTrackCallback
            featureCalc.onReceivePoints(
                FeatureCalc.getLeftEye(it[0].landmarks),
                FeatureCalc.getRightEye(it[0].landmarks),
                FeatureCalc.getMouth(it[0].landmarks)
            )
        }


        socketKeeper = SocketKeeper(listener)

        socketKeeper.connect()

        return root

    }

    fun setFatigueBannerVisible(visible: Boolean) {
        fatigueBanner.visibility = if (visible) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }

    fun setFatigueMsg(isFatigue: Boolean) {
        if (isFatigue) {
            val uri = Uri.parse("android.resource://link.dayang.rtmpdemo/" + R.raw.bell_short);

            val tone = RingtoneManager.getRingtone(requireContext(), uri)
            tone.play()
            msg.text = "您已达到疲劳状态"
        } else {
            msg.text = "Standing by"

        }

    }

    fun toast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    companion object {
        init {
            System.loadLibrary("Tracking-lib")

        }
    }
}