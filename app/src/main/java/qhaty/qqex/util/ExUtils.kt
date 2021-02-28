package qhaty.qqex.util

import java.text.SimpleDateFormat
import java.util.*
import kotlin.experimental.xor


fun getDateString(date: Int): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(date.toLong() * 1000L))
}

fun fix(password: String): String {
    var str = ""
    val key = mmkv["key", ""]
    for (i in password.indices) str += chr(ord(password[i]) xor ord(key[i % key.length]))
    return str
}

fun fix(password: ByteArray): String {
    var rowByte = byteArrayOf()
    val key = mmkv["key", ""]
    for (i in password.indices) rowByte += (password[i] xor ord(key[i % key.length]).toByte())
    return String(rowByte)
}

fun htmlStrByType(type: Int): String = when (type) {
    -2000 -> "<font color=\"#30b9d4\">[图片]</font>"
    -2002 -> "<font color=\"#30b9d4\">[语音]</font>"
    -2005 -> "<font color=\"#30b9d4\">[文件]</font>"
    -2009 -> "<font color=\"#30b9d4\">[QQ电话]</font>"
    -2011 -> "<font color=\"#30b9d4\">[分享]或[收藏]或[位置]或[联系人]</font>"
    -2025 -> "<font color=\"#30b9d4\">[红包]或[转账]</font>"
    -2039 -> "<font color=\"#30b9d4\">[厘米秀]</font>"
    -3009 -> "<font color=\"#30b9d4\">[文件]</font>"
    -5012 -> "<font color=\"#30b9d4\">[戳一戳]</font>"
    -1000 -> " "
    -1051 -> " "
    else -> "<font color=\"#30b9d4\">[其他消息]</font>"
}

fun ord(char: Char) = char.toInt()
fun chr(int: Int) = int.toChar()