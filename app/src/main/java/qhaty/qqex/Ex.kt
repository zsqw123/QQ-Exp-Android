package qhaty.qqex

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.experimental.xor

class Ex {
    private val allChat = arrayListOf<HashMap<String, Any>>()
    private val allChatDecode = arrayListOf<HashMap<String, Any>>()
    private var htmlStr = "<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /></head>"
    private var saveStr = ""
    fun startEx(context: Context) {
        GlobalScope.launch {
            val dbFileList = GetDB(context).getDataBase()
            doEx(dbFileList!![0], dbFileList[1], context)
        }
    }

    private suspend fun doEx(libFileNew: File, libFileOld: File?, context: Context) {
        withContext(Dispatchers.IO) {
            val old = async(Dispatchers.IO) { if (libFileOld != null) addDByPath(libFileOld) }
            val new = async(Dispatchers.IO) { addDByPath(libFileNew) }
            old.await()
            new.await()
            chatsDecode()
            toHtml()
            launch(Dispatchers.IO) { textToDownload(context, Data.friendQQ, htmlStr) }
            launch(Dispatchers.IO) { textToAppData(context, Data.friendQQ, saveStr) }
        }
    }

    private fun addDByPath(libFile: File) {
        val oldSql = SQLiteDatabase.openDatabase(libFile.absolutePath, null, SQLiteDatabase.OPEN_READWRITE)
        val friendOrTroop = if (Data.friendOrGroup) "friend" else "troop"
        val sqlDo = "SELECT _id,msgData,msgtype,senderuin,time FROM mr_${friendOrTroop}_" +
                "${encodeMD5(Data.friendQQ).toUpperCase(Locale.ROOT)}_New ORDER BY time ASC;"
        val cursorOld = oldSql.rawQuery(sqlDo, null)
        if (cursorOld.count > 1) cursorOld.moveToFirst()
        do {
            val single = HashMap<String, Any>()
            single["data"] = cursorOld.getBlob(1)
            single["type"] = cursorOld.getInt(2)
            single["sender"] = cursorOld.getString(3)
            single["time"] = cursorOld.getInt(4)
            allChat.add(single)
        } while (cursorOld.moveToNext())
        oldSql.close()
        cursorOld.close()
    }

    private fun chatsDecode() {
        val single = hashMapOf<String, Any>()
        allChat.forEach {
            single["time"] = it["time"] as Int
            single["type"] = it["type"] as Int
            single["sender"] = fix(other = it["sender"] as String)
            single["data"] = fix(it["data"] as ByteArray)
            allChatDecode += single
        }
        allChatDecode.sortBy { it["time"] as Int }
    }

    private fun toHtml() {
        for (i in allChatDecode) {
            saveStr += i["data"] as String
            try {
                htmlStr = "$htmlStr<font color=\"blue\">${getDateString(i["time"] as Int)}" +
                        "</font>-----<font color=\"green\">${i["sender"] as String}</font>" +
                        "</br>${htmlStrByType(i["type"] as Int) + i["data"] as String}</br></br>"
            } catch (e: Exception) {
                continue
            }
        }
    }
}

@Throws(IOException::class)
fun initImgFile(path: String) {
    val file = File(path)
    if (!file.parentFile?.exists()!!) {
        file.parentFile?.mkdirs()
    }
    val imgFiles = File(file.parentFile!!.absolutePath + "/images")
    if (!imgFiles.exists()) {
        imgFiles.mkdirs()
    }
    if (file.exists()) {
        file.delete()
    }
    file.createNewFile()
}

fun getDateString(date: Int): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(date.toLong() * 1000L))
}

fun fix(msgData: ByteArray? = null, other: String? = null): String {
    if (msgData != null) {
        var rowByte = byteArrayOf()
        for (i in msgData.indices) rowByte += msgData[i] xor Data.key[i % Data.key.length].toByte()
        return String(rowByte, charset("UTF-8"))
    } else if (other != null) {
        var str = ""
        val bytes = other.toByteArray(charset("UTF-8"))
        for (i in bytes.indices) str += bytes[i] xor Data.key[i % Data.key.length].toByte()
        return str
    }
    return ""
}

fun htmlStrByType(type: Int): String = when (type) {
    -1000 -> ""
    -1051 -> ""
    -2000 -> "[图片]"
    -2002 -> "<font color=\"#30b9d4\">[语音]</font>"
    -2005 -> "<font color=\"#30b9d4\">[文件]</font>"
    -2009 -> "<font color=\"#30b9d4\">[QQ电话]</font>"
    -2011 -> "<font color=\"#30b9d4\">[分享]或[收藏]或[位置]或[联系人]</font>"
    -2025 -> "<font color=\"#30b9d4\">[红包]或[转账]</font>"
    -2039 -> "<font color=\"#30b9d4\">[厘米秀]</font>"
    -3009 -> "<font color=\"#30b9d4\">[文件]</font>"
    -5012 -> "<font color=\"#30b9d4\">[戳一戳]</font>"
    else -> "[其他类型消息]"
}