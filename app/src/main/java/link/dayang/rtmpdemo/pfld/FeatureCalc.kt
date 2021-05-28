package link.dayang.rtmpdemo.pfld

import android.graphics.PointF
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

// maxMouth为次数 需换算 1秒30帧 1秒20次
class FeatureCalc(
    private val onCalc: (P70: Float, maxMouth: Int) -> Unit
) {

    private var countTime = 0 //量化总次数

    private var countCloseEye = 0 //量化内闭眼次数

    private var maxMouth = 0 //张嘴结算后量化内最大张嘴次数数
    private var countOpenMouth = 0 //当前张嘴过程张嘴次数数
    private var isMouthOpening = false //正在张嘴



    fun onReceivePoints(leftEye: List<PointF>, rightEye: List<PointF>, mouth: List<PointF>) {
        countTime++
        val eyeClose = checkEyeClose(ratio(leftEye), ratio(rightEye))
        if (eyeClose) countCloseEye++

        val mouthOpen = checkMonthOpen(ratio(mouth))
        if (mouthOpen) {
            if (isMouthOpening) {
                countOpenMouth++
            } else {
                isMouthOpening = true
            }
            maxMouth = max(maxMouth, countOpenMouth)
        } else {
            isMouthOpening = false
            countOpenMouth = 0
        }

        if (countTime >= MAXTIME) {
            if (!eyeClose && !mouthOpen) {
                val P70 = countCloseEye.toFloat() / countTime.toFloat()
                val curMaxMouth = maxMouth
                onCalc(P70, curMaxMouth)

                countTime = 0
                countCloseEye = 0
                maxMouth = 0
            }
        }



    }
//
//    fun getCurP70(): Float {
////        return
//    }
//
//    fun getCurMaxMouth(): Int {
//        return maxMouth
//    }

    companion object {
        const val MAXTIME = 40

//        private const val thresholdEyes = 0.25F
        private const val thresholdEyes = 0.2F // 眼睛小瞪不到0.25*2，改到0.2*2
        private const val thresholdMouth = 0.40F

        private val indexLeftEye = listOf(94, 1, 53, 59, 67, 12)
        private val indexRightEye = listOf(27, 104, 85, 20, 47, 51)
        private val indexMouth = listOf(61, 40, 25, 42, 2, 63)

        fun checkEyeClose(leftEyeRatio: Float, rightEyeRatio: Float): Boolean {
            return (leftEyeRatio + rightEyeRatio) < (thresholdEyes * 2)
        }

        fun checkMonthOpen(mouthRatio: Float): Boolean {
            return (mouthRatio > thresholdMouth)
        }

        fun ratio(contour: List<PointF>): Float {
            val a = euclidean(contour[1], contour[5])
            val b = euclidean(contour[2], contour[4])
            val c = euclidean(contour[0], contour[3])
            return (a + b) / (2.0F * c)
        }

        fun euclidean(p: PointF, q: PointF): Float {
            return sqrt((p.x - q.x).pow(2) + (p.y - q.y).pow(2))
        }

        fun getLeftEye(landmarks: IntArray): List<PointF> {
            val list = mutableListOf<PointF>()
            indexLeftEye.forEach {
                list.add(PointF(landmarks[it * 2].toFloat(), landmarks[it * 2 + 1].toFloat()))
            }
            return list
        }

        fun getRightEye(landmarks: IntArray): List<PointF> {
            val list = mutableListOf<PointF>()
            indexRightEye.forEach {
                list.add(PointF(landmarks[it * 2].toFloat(), landmarks[it * 2 + 1].toFloat()))
            }
            return list
        }

        fun getMouth(landmarks: IntArray): List<PointF> {
            val list = mutableListOf<PointF>()
            indexMouth.forEach {
                list.add(PointF(landmarks[it * 2].toFloat(), landmarks[it * 2 + 1].toFloat()))
            }
            return list
        }
    }
}