package qhaty.qqex

import android.content.Context
import com.jaredrummler.android.shell.Shell
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
                runOnUI { context.toast("无法获取聊天数据文件") }
                null
            }
        } else {
            runOnUI { context.toast("无内置储存") }
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
            runOnUI { context.toast("无root权限! 若已授权，请忽略") }
        }
    }
}