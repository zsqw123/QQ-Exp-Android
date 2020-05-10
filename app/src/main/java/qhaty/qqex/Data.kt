package qhaty.qqex

import com.chibatching.kotpref.KotprefModel

object Data : KotprefModel() {
    var key by stringPref()
    var meQQ by stringPref()
    var friendQQ by stringPref()
    var friendOrGroup by booleanPref(true) // true is friend
    var needPic by booleanPref(false)
    var hasRoot by booleanPref(false)
}