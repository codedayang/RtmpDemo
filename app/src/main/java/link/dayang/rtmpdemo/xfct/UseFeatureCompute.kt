package link.dayang.rtmpdemo.xfct

import android.util.Log
import link.dayang.rtmpdemo.pfld.FeatureCalc
import zeusees.tracking.Face
import java.lang.StringBuilder
import java.util.concurrent.ThreadPoolExecutor

class UseFeatureCompute(
    private val service: DetectorService,
    private val usePfld: UsePfld,
    private val onFeatureAvailable: (P70: Float, maxMouth: Int) -> Unit
) {

    private val featureCalc = FeatureCalc { P70, maxMouth ->
        val builder = StringBuilder()
        builder.append(P70)
        builder.append("\n")
        builder.append(maxMouth.toFloat() / 20F * 30F)
        val statusText = builder.toString()
        Log.v("dydy", statusText)
        onFeatureAvailable(P70, maxMouth)

//        ThreadPoolExecutor
        

//        socketKeeper.send(P70, maxMouth)
    }

    fun create() {
        usePfld.registTrackCallback( object : UsePfld.TrackCallBack {
            override fun onTrackdetectedFace(faces: List<Face>) {
                if (faces.isEmpty()) return
                featureCalc.onReceivePoints(
                    FeatureCalc.getLeftEye(faces[0].landmarks),
                    FeatureCalc.getRightEye(faces[0].landmarks),
                    FeatureCalc.getMouth(faces[0].landmarks)
                )
            }
        })
    }

    fun release() {
        usePfld.registTrackCallback(null)
    }

    fun start() {

    }

    fun stop() {
    }
}