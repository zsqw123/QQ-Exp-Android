package qhaty.qqex.method

import android.annotation.SuppressLint
import android.app.Activity
import android.database.sqlite.SQLiteDatabase
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.*
import qhaty.qqex.*
import qhaty.qqex.ui.expWithRebuildDialog
import qhaty.qqex.util.*
import java.io.File
import java.util.*

class Ex(private val tv: TextView, private val lifecycleScope: LifecycleCoroutineScope, private val activity: Activity) {
    class Progress(var per: Int, @StringRes var msg: Int)

    private var progress: Progress = Progress(0, R.string.awa)
        set(value) {
            field = value
            val s: String = String.format("%.1f %%\n%s", value.per / 10.0, activity.resources.getString(value.msg))
            lifecycleScope.launch(Dispatchers.Main) { tv.text = s }
        }

    suspend fun start(end: () -> Unit) {
        progress = Progress(0, R.string.start_ex)
        val database: MutableList<File>? = getLocalDB()?.toMutableList()
        if (database != null) activity.expWithRebuildDialog(lifecycleScope) {
            if (!database[0].exists()) {
                val isCopied = copyUseRoot()
                if (!isCopied) {
                    progress = Progress(0, R.string.no_db)
                    return@expWithRebuildDialog
                }
            }
            export()
            end()
        } else {
            val isCopied = copyUseRoot()
            if (!isCopied) {
                progress = Progress(0, R.string.no_db)
                return
            }
            export()
            end()
        }

    }

    private suspend fun export() {
        val database = getLocalDB()?.toMutableList()
        if (database == null) {
            progress = Progress(0, R.string.no_db)
            return
        }
        progress = Progress(150, R.string.open_db)
        val allCodedChat = ArrayList<CodedChat>()
        withContext(Dispatchers.IO) { addDByPath(database[0])?.let { allCodedChat.addAll(it) } } // 新数据库
        if (database.size > 1) withContext(Dispatchers.IO) { addDByPath(database[1])?.let { allCodedChat.addAll(it) } } // 旧数据库
        progress = Progress(250, R.string.decode_db)
        val allChats = chatsDecode(allCodedChat) // 解码数据库
        progress = Progress(560, R.string.ex_html)
        toHtml(allChats)
        progress = Progress(1000, R.string.save_ok)
        withContext(Dispatchers.Main) {
            saveHtmlFile?.let { activity.sendToViewHtml(it) }
            toast("文件保存至:Android/data/qhaty.qqex/files/savedHtml")
        }
    }

    @SuppressLint("Recycle")
    private fun addDByPath(libFile: File): ArrayList<CodedChat>? {
        val chats = arrayListOf<CodedChat>()
        val sql = SQLiteDatabase.openDatabase(libFile.absolutePath, null, 0)
        val friendOrTroop = if (mmkv["friendOrGroup", true]) "friend" else "troop"
        val sqlDo = "SELECT _id,msgData,msgtype,senderuin,time FROM mr_${friendOrTroop}_" +
                "${encodeMD5(mmkv["exQQ", ""]).toUpperCase(Locale.ROOT)}_New"
        try {
            val cursor = try {
                sql.rawQuery(sqlDo, null)
            } catch (e: java.lang.Exception) {
                null
            } ?: return null
            if (cursor.count > 1) cursor.moveToFirst()
            do {
                val data = cursor.getBlob(1)
                val type = cursor.getInt(2)
                val sender = cursor.getString(3)
                val time = cursor.getInt(4)
                chats += CodedChat(time, type, sender, data)
            } while (cursor.moveToNext())
            cursor.close()
        } catch (e: java.lang.Exception) {
        }
        sql.close()
        return chats
    }

    private suspend fun chatsDecode(allChat: List<CodedChat>): ArrayList<Chat> {
        return withContext(Dispatchers.Default) {
            val allChatDecode = arrayListOf<Chat>()
            val allCount = allChat.size
            // 昵称配对 map
            val nicknameSet: Set<String> = mmkv["nickname_parse", emptySet()]
            val qqMap = hashMapOf<String, String>()
            val regex1 = Regex(""".*?--QQEX--""")
            val regex2 = Regex("""--QQEX--(.*?)-""")
            nicknameSet.forEach {
                val r0 = it.replace("--QQS--", "").replace("-QQE--", "")
                val qq = regex1.find(r0)?.value?.replace("--QQEX--", "")
                val name = regex2.find(r0)?.value?.replace("--QQEX--", "")?.replace("-", "")
                if (!qq.isNullOrBlank() && !name.isNullOrBlank()) qqMap[qq] = name
            }
            // 聊天解码
            for (i in allChat.indices) {
                val time = allChat[i].time
                val type = allChat[i].type
                var fixedQQ: String = fix(allChat[i].sender)
                for ((k, v) in qqMap) fixedQQ = fixedQQ.replace(k, v)
                val sender = fixedQQ
                val data = fix(allChat[i].msg)
                allChatDecode += Chat(time, type, sender, data)
                if (i % 20 == 0) progress = progress.apply { per = ((i.toFloat() / allCount) * 300 + 250).toInt() }
            }
            allChatDecode.sortBy { it.time }
            return@withContext allChatDecode
        }
    }

    private suspend fun toHtml(allChatDecode: ArrayList<Chat>) {
        withContext(Dispatchers.Default) {
            val head = "<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /></head>"
            var appendHtml = ""
            var appendStr = ""
            withContext(Dispatchers.IO) {
                val path0 = application.getExternalFilesDir("savedHtml")
                File(path0, "${mmkv["exQQ", ""]}.html").apply { if (exists()) delete() }
                val path1 = application.getExternalFilesDir("words")
                File(path1, mmkv["exQQ", ""]).apply { if (exists()) delete() }
            }
            for (i in allChatDecode.indices) {
                if (i == 0) appendHtml += head
                val item = allChatDecode[i]
                try {
                    val htmlByTypeStr = htmlStrByType(item.type)
                    val msg = if (htmlByTypeStr != " ") htmlByTypeStr else {
                        appendStr += item.msg
                        item.msg
                    }
                    appendHtml = "$appendHtml<font color=\"blue\">${getDateString(item.time)}" +
                            "</font>-----<font color=\"green\">${item.sender}</font>" +
                            "</br>$msg</br></br>"
                } catch (e: Exception) {
                    continue
                }
                if (i % 30 == 0) { //每 30 条保存一次
                    appendTextToAppDownload(application, mmkv["exQQ", ""], appendHtml)
                    appendHtml = ""
                    appendTextToAppData(application, mmkv["exQQ", ""], appendStr)
                    appendStr = ""
                    progress = progress.apply { per = ((i.toFloat() / allChatDecode.size) * 650 + 300).toInt() }
                } else if (i == allChatDecode.size - 1) {
                    appendTextToAppDownload(application, mmkv["exQQ", ""], appendHtml)
                    appendTextToAppData(application, mmkv["exQQ", ""], appendStr)
                }
            }
        }
    }
}