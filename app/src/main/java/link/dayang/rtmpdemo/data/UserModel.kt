package link.dayang.rtmpdemo.data

import com.chibatching.kotpref.KotprefModel

object UserModel : KotprefModel() {
    var token by stringPref("")

}