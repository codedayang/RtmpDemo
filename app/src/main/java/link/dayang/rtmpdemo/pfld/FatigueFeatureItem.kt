package link.dayang.rtmpdemo.pfld

data class FatigueFeatureItemReq(
    val token: String,
    val uuid: String,
    val P_70: Float,
    val max_mouth: Int,
    val timestamp: Long
)

data class FatigueFeatureItemRes(
    val uuid: String,
    val pred: Float,
    val timestamp: Long
)
