package qhaty.qqex.method

import android.content.Context
import com.jaredrummler.android.shell.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import qhaty.qqex.Data
import qhaty.qqex.R
import qhaty.qqex.application
import qhaty.qqex.util.get
import qhaty.qqex.util.mmkv
import qhaty.qqex.util.runOnUI
import qhaty.qqex.util.toast
import java.io.File

class GetDB(private var context: Context) {
    fun getDataBase(): List<File>? {
        val qqNumber = Data.meQQ
        val dir = context.getExternalFilesDir(null)
        val dbFileNew: File
        val dbFileOld: File

        if (dir != null && qqNumber != "") {
            if (!dir.exists()) dir.mkdirs()
            dbFileNew = File(dir.absolutePath + "/$qqNumber.db")
            dbFileOld = File(dir.absolutePath + "/slowtable_$qqNumber.db")
            return if (dbFileNew.exists()) {
                if (dbFileOld.exists()) listOf(dbFileNew, dbFileOld) else listOf(dbFileNew)
            } else if (Data.hasRoot) {
                copyUseRoot()
                if (dbFileOld.exists()) listOf(dbFileNew, dbFileOld) else listOf(dbFileNew)
            } else {
                runOnUI { toast("无法获取聊天数据文件") }
                null
            }
        } else {
            runOnUI { toast("无内置储存") }
            return null
        }
    }

    private fun copyUseRoot() {
        try {
            if (Data.hasRoot)
                if (Shell.SU.available()) {
                    val dir = context.getExternalFilesDir(null)!!
                    val qqPkg = "com.tencent.mobileqq"
                    val cmd0 = "am force-stop $qqPkg"
                    val cmd1 =
                        "cp -f /data/data/$qqPkg/databases/${Data.meQQ}.db ${dir.absolutePath}/${Data.meQQ}.db"
                    val cmd2 =
                        "cp -f /data/data/$qqPkg/databases/slowtable_${Data.meQQ}.db ${dir.absolutePath}/slowtable_${Data.meQQ}.db"
                    Shell.SU.run(cmd0, cmd1, cmd2)
                }
        } catch (e: Exception) {
            runOnUI { toast("无root权限! 若已授权，请忽略") }
        }
    }
}

suspend fun getLocalDB(): List<File>? {
    val qqNumber = mmkv["myQQ", ""]
    val dir = application.getExternalFilesDir(null)
    val dbFileNew: File
    val dbFileOld: File

    return withContext(Dispatchers.IO) {
        if (dir != null && qqNumber != "") {
            if (!dir.exists()) dir.mkdirs()
            dbFileNew = File(dir.absolutePath + "/$qqNumber.db")
            dbFileOld = File(dir.absolutePath + "/slowtable_$qqNumber.db")
            return@withContext if (dbFileNew.exists()) {
                if (dbFileOld.exists()) listOf(dbFileNew, dbFileOld) else listOf(dbFileNew)
            } else null
        } else {
            toast("无内置储存")
            return@withContext null
        }
    }
}

suspend fun copyUseRoot(): Boolean {
    try {
        var successFlag = false
        if (mmkv["root", false]) withContext(Dispatchers.IO) {
            if (Shell.SU.available()) {
                val dir = application.getExternalFilesDir(null)!!
                val qqPkg = "com.tencent.mobileqq"
                val cmd0 = "am force-stop $qqPkg"
                val cmd1 =
                    "cp -f /data/data/$qqPkg/databases/${Data.meQQ}.db ${dir.absolutePath}/${Data.meQQ}.db"
                val cmd2 =
                    "cp -f /data/data/$qqPkg/databases/slowtable_${Data.meQQ}.db ${dir.absolutePath}/slowtable_${Data.meQQ}.db"
                Shell.SU.run(cmd0, cmd1)
                successFlag = true
                Shell.SU.run(cmd2)
            }
        } else toast(R.string.open_root)
        return successFlag
    } catch (e: Exception) {
        toast(R.string.maybe_no_root)
        return false
    }
}