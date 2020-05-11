package qhaty.qqex

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.view.View
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.experimental.xor
import kotlin.properties.Delegates

class Ex {
    private var htmlStr = "<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /></head>"
    private var saveStr = ""
    private var progress: Progress by Delegates.observable(
        Progress(0, "开始导出...")
    ) { _, _, new -> onProgressChange(new) }

    fun startEx(context: Context, keyGenText: String? = null) {
        GlobalScope.launch(Dispatchers.Default) {
            progress = Progress(10, "读取数据库...")
            val dbFileList: List<File>? = GetDB(context).getDataBase()
            if (dbFileList == null) {
                progress = Progress(progress.progress, "无法读取数据库\n请手动导入")
                return@launch
            }
            progress = Progress(50, "导出数据库...")
            doEx(dbFileList[0], dbFileList[1], context, keyGenText)
        }
    }

    private suspend fun doEx(libFileNew: File, libFileOld: File?, context: Context, keyGenText: String? = null) {
        withContext(Dispatchers.Default) {
            val old =
                withContext(Dispatchers.IO) { if (libFileOld != null) addDByPath(libFileOld) else null }
            val new = withContext(Dispatchers.IO) { addDByPath(libFileNew, keyGenText) }
            if (old != null) new += old
            progress = Progress(200, "解析数据库...")
            toHtml(withContext(Dispatchers.Default) { chatsDecode(new) })
            progress = Progress(progress.progress, "保存网页到本地中...")
            textToDownload(context, Data.friendQQ, htmlStr)
            textToAppData(context, Data.friendQQ, saveStr)
            progress = Progress(1000, "保存成功")
        }
    }

    private fun addDByPath(libFile: File, keyGenText: String? = null): ArrayList<HashMap<String, Any>> {
        val chats = arrayListOf<HashMap<String, Any>>()
        val sql = SQLiteDatabase.openDatabase(libFile.absolutePath, null, 0)
        val friendOrTroop = if (Data.friendOrGroup) "friend" else "troop"
        val sqlDo = "SELECT _id,msgData,msgtype,senderuin,time FROM mr_${friendOrTroop}_" +
                "${encodeMD5(Data.friendQQ).toUpperCase(Locale.ROOT)}_New"
        try {
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
                chats += single
                if (first && keyGen) {
                    dbLastData = single["data"] as ByteArray
                    decodeKey(keyGenText!!)
                    first = false
                }
            } while (cursor.moveToNext())
            cursor.close()
        } catch (e: java.lang.Exception) {
        }
        sql.close()
        return chats
    }

    private fun chatsDecode(allChat: ArrayList<HashMap<String, Any>>): ArrayList<HashMap<String, Any>> {
        val allChatDecode = arrayListOf<HashMap<String, Any>>()
        val single = hashMapOf<String, Any>()
        val allCount = allChat.size
        progress = Progress(progress.progress, "数据库解码...")
        for (i in allChat.indices) {
            single["time"] = allChat[i]["time"] as Int
            single["type"] = allChat[i]["type"] as Int
            single["sender"] = fix(other = allChat[i]["sender"] as String)
            single["data"] = fix(allChat[i]["data"] as ByteArray)
//            println(single["data"])
            allChatDecode += single
            if (i % 20 == 0) {
                progress = Progress(((i.toFloat() / allCount) * 500 + 200).toInt(), progress.msg)
            }
        }
        allChatDecode.sortBy { it["time"] as Int }
        return allChatDecode
    }

    private fun toHtml(allChatDecode: ArrayList<HashMap<String, Any>>) {
        progress = Progress(progress.progress, "导出网页...")
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
                progress = Progress(((i.toFloat() / allChatDecode.size) * 250 + 700).toInt(), progress.msg)
            }
        }
    }

    private fun onProgressChange(new: Progress) {
        runOnUI {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    ProgressView.progressView?.setProgress(new.progress, true)
                } else {
                    ProgressView.progressView?.progress = new.progress
                }
                ProgressView.progressText?.text = new.msg
                if (new.progress > 990) {
                    ProgressView.dialog?.apply {
                        setCanceledOnTouchOutside(true)
                        setCancelable(true)
                    }
                    ProgressView.cirProgress?.visibility = View.GONE
                }
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
        for (i in msgData.indices) rowByte += msgData[i] xor ord(Data.key[i % Data.key.length]).toByte()
        return String(rowByte, charset("UTF-8"))
    } else if (other != null) {
        var str = ""
        for (i in other.indices) str += chr(ord(other[i]) xor ord(Data.key[i % Data.key.length]))
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
    val msgEnc = str.toByteArray()
    var keyS = ""
    for (i in str.indices) keyS += chr((dbLastData[i] xor msgEnc[i]).toInt())
    var realK = ""
    var nextK = ""
    var restK = ""
    for (i in 4 until keyS.length) {
        for (j in 0..i) realK += keyS[j]
        for (j in i..2 * i) catchError { nextK += keyS[j] }
        if (2 * i < keyS.length) for (j in 2 * i until keyS.length) restK += keyS[j]
        var flagLoop = true
        for (j in realK.indices) {
            if ((j < nextK.length && realK[j] != nextK[j])
                || (j < restK.length && realK[j] != restK[j])
            ) {
                flagLoop = false
                break
            }
        }
        if (flagLoop) break
    }
    return realK
}

fun ord(char: Char) = char.toInt()
fun chr(int: Int) = int.toChar()
fun catchError(method: () -> Unit) {
    try {
        method()
    } catch (e: java.lang.Exception) {
    }
}