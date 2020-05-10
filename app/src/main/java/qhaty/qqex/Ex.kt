package qhaty.qqex

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

class Ex {
    private val allChat = arrayListOf<HashMap<String, Any>>()
    fun startEx(context: Context) {
        GlobalScope.launch {
            val dbFileList = GetDB(context).getDataBase()
            doEx(dbFileList!![0], dbFileList[1])
        }
    }

    private suspend fun doEx(libFileNew: File, libFileOld: File?): File? {
        withContext(Dispatchers.IO) {
            val old = async(Dispatchers.IO) { if (libFileOld != null) addDByPath(libFileOld) }
            val new = async(Dispatchers.IO) { addDByPath(libFileNew) }
            old.await()
            new.await()

        }
        return null
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
            single["sender"] = cursorOld.getString(3)
            single["type"] = cursorOld.getInt(2)
            single["data"] = cursorOld.getBlob(1)
            single["time"] = cursorOld.getInt(4)
            allChat.add(single)
        } while (cursorOld.moveToNext())
        oldSql.close()
        cursorOld.close()
    }
}

@Throws(IOException::class)
fun initFile(path: String) {
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