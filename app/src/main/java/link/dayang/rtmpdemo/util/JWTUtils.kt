package link.dayang.rtmpdemo.util

import com.auth0.android.jwt.JWT
import java.nio.charset.Charset
import java.util.*

object JWTUtils {
    fun getUserSub(jwtEncoded: String): String {
        val jwt = JWT(jwtEncoded)
        return jwt.getClaim("sub").asString()?:"-1"
    }
}