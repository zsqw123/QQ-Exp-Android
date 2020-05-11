package qhaty.qqex

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.experimental.xor
import kotlin.properties.Delegates

class Ex {
    private val allChat = arrayListOf<HashMap<String, Any>>()
    private val allChatDecode = arrayListOf<HashMap<String, Any>>()
    private var htmlStr = "<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /></head>"
    private var saveStr = ""
    private var progress: Progress by Delegates.observable(
        Progress(0, "开始导出...")
    ) { _, _, new -> onProgressChange(new) }

    fun startEx(context: Context, keyGenText: String? = null) {
        GlobalScope.launch {
            progress = Progress(10, "读取数据库...")
            val dbFileList = GetDB(context).getDataBase()
            progress = Progress(50, "导出数据库...")
            doEx(dbFileList!![0], dbFileList[1], context, keyGenText)
        }
    }

    private suspend fun doEx(libFileNew: File, libFileOld: File?, context: Context, keyGenText: String? = null) {
        withContext(Dispatchers.IO) {
            val old = async(Dispatchers.IO) { if (libFileOld != null) addDByPath(libFileOld, keyGenText) }
            val new = async(Dispatchers.IO) { addDByPath(libFileNew, keyGenText) }
            old.await()
            progress = Progress(120, "解析数据库...")
            new.await()
            progress.progress = 200
            chatsDecode()
            toHtml()
            progress.msg = "保存网页到本地中..."
            val toDownload = async(Dispatchers.IO) { textToDownload(context, Data.friendQQ, htmlStr) }
            val toData = async(Dispatchers.IO) { textToAppData(context, Data.friendQQ, saveStr) }
            toData.await()
            toDownload.await()
            progress.progress = 1000
            progress.msg = "保存成功"
        }
    }

    private fun addDByPath(libFile: File, keyGenText: String? = null) {
        val sql = SQLiteDatabase.openDatabase(libFile.absolutePath, null, SQLiteDatabase.OPEN_READWRITE)
        val friendOrTroop = if (Data.friendOrGroup) "friend" else "troop"
        val sqlDo = "SELECT _id,msgData,msgtype,senderuin,time FROM mr_${friendOrTroop}_" +
                "${encodeMD5(Data.friendQQ).toUpperCase(Locale.ROOT)}_New ORDER BY time ASC;"
        val cursor = sql.rawQuery(sqlDo, null)
        if (cursor.count > 1) cursor.moveToFirst()
        var first = true
        val keyGen: Boolean = keyGenText != null
        do {
            val single = HashMap<String, Any>()
            single["data"] = cursor.getBlob(1)
            single["type"] = cursor.getInt(2)
            single["sender"] = cursor.getString(3)
            single["time"] = cursor.getInt(4)
            allChat.add(single)
            if (first && keyGen) {
                dbLastData = single["data"] as ByteArray
                decodeKey(keyGenText!!)
                first = false
            }
        } while (cursor.moveToNext())
        sql.close()
        cursor.close()
    }

    private fun chatsDecode() {
        val single = hashMapOf<String, Any>()
        val allCount = allChat.size
        progress.msg = "数据库解码..."
        for (i in allChat.indices) {
            single["time"] = allChat[i]["time"] as Int
            single["type"] = allChat[i]["type"] as Int
            single["sender"] = fix(other = allChat[i]["sender"] as String)
            single["data"] = fix(allChat[i]["data"] as ByteArray)
            allChatDecode += single
            if (i % 20 == 0) {
                progress.progress = ((i.toFloat() / allCount) * 500 + 200).toInt()
            }
        }
        allChatDecode.sortBy { it["time"] as Int }
    }

    private fun toHtml() {
        progress.msg = "导出网页..."
        for (i in allChatDecode.indices) {
            val item = allChatDecode[i]
            saveStr += item["data"] as String
            try {
                htmlStr = "$htmlStr<font color=\"blue\">${getDateString(item["time"] as Int)}" +
                        "</font>-----<font color=\"green\">${item["sender"] as String}</font>" +
                        "</br>${htmlStrByType(item["type"] as Int) + item["data"] as String}</br></br>"
            } catch (e: Exception) {
                continue
            }
            if (i % 20 == 0) {
                progress.progress = ((i.toFloat() / allChatDecode.size) * 250 + 700).toInt()
            }
        }
    }

    private fun onProgressChange(new: Progress) {
        runOnUI {
            try {
                ProgressView.progressView?.progress = new.progress
                ProgressView.progressText?.text = new.msg
            } catch (e: java.lang.Exception) {
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
    -2000 -> "[图片]"
    -2002 -> "<font color=\"#30b9d4\">[语音]</font>"
    -2005 -> "<font color=\"#30b9d4\">[文件]</font>"
    -2009 -> "<font color=\"#30b9d4\">[QQ电话]</font>"
    -2011 -> "<font color=\"#30b9d4\">[分享]或[收藏]或[位置]或[联系人]</font>"
    -2025 -> "<font color=\"#30b9d4\">[红包]或[转账]</font>"
    -2039 -> "<font color=\"#30b9d4\">[厘米秀]</font>"
    -3009 -> "<font color=\"#30b9d4\">[文件]</font>"
    -5012 -> "<font color=\"#30b9d4\">[戳一戳]</font>"
    else -> ""
}

var dbLastData = byteArrayOf()
fun decodeKey(str: String): String {
    var keySet = byteArrayOf()
    val strArrays = str.toByteArray(Charsets.UTF_8)
    for (i in strArrays.indices) keySet += dbLastData[i] xor strArrays[i]
    val realKey = byteArrayOf()
    val nextKey = byteArrayOf()
    val restKey = byteArrayOf()
    for (i in 4..keySet.size) {
        for (j in 0..i) realKey[j] = keySet[j]
        for (j in i..2 * i) if (2 * i <= keySet.size - 1) nextKey[j] = keySet[j]
        if (2 * i < keySet.size) for (j in 2 * i until keySet.size) restKey[j] = keySet[j]
        var flagLoop = true
        for (j in realKey.indices) {
            if ((j < nextKey.size && realKey[j] != nextKey[j])
                || (j < restKey.size && realKey[j] != restKey[j])
            ) {
                flagLoop = false
                break
            }
        }
        if (flagLoop) break
    }
    val keyStr = String(realKey, Charsets.UTF_8)
    if (Data.key.length < 4) Data.key = keyStr
    return keyStr
}