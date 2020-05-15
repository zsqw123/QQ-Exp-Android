package qhaty.qqex

import com.chibatching.kotpref.KotprefModel

object Data : KotprefModel() {
    var key by stringPref()
    var meQQ by stringPref()
    var friendQQ by stringPref()
    var friendOrGroup by booleanPref(true) // true is friend
    var needPic by booleanPref(false)
    var hasRoot by booleanPref(false)
    var keyType by intPref(0)
}

object QQNickNameParse : KotprefModel() {
    val dataSet by stringSetPref()
}

data class Word(var str: String, var weight: Int = 1)