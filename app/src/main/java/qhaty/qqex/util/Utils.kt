package qhaty.qqex.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import qhaty.qqex.application
import java.io.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

fun toast(str: String) {
    GlobalScope.launch(Dispatchers.Main) {
        Toast.makeText(application, str, Toast.LENGTH_SHORT).show()
    }
}

fun toast(@StringRes id: Int) {
    toast(application.resources.getString(id))
}

fun encodeMD5(text: String): String {
    try {
        val instance: MessageDigest = MessageDigest.getInstance("MD5")
        val digest: ByteArray = instance.digest(text.toByteArray())
        val sb = StringBuffer()
        for (b in digest) {
            //获取低八位有效值
            val i: Int = b.toInt() and 0xff
            //将整数转化为16进制
            var hexString = Integer.toHexString(i)
            if (hexString.length < 2) {
                //如果是一位的话，补0
                hexString = "0$hexString"
            }
            sb.append(hexString)
        }
        return sb.toString()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }
    return ""
}

var saveHtmlFile: File? = null
suspend fun appendTextToAppDownload(context: Context, fileName: String, text: String) {
    withContext(Dispatchers.IO) {
        if (saveHtmlFile == null) {
            val path = context.getExternalFilesDir("savedHtml")
            saveHtmlFile = File("$path/$fileName.html")
        }
        var out: BufferedWriter? = null
        try {
            out = BufferedWriter(OutputStreamWriter(FileOutputStream(saveHtmlFile, true)))
            out.write(text)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            try {
                out?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

}

var saveWordFile: File? = null
suspend fun appendTextToAppData(context: Context, fileName: String, text: String) {
    withContext(Dispatchers.IO) {
        if (saveWordFile == null) {
            val path = context.getExternalFilesDir("words")
            if (path != null) {
                if (!path.exists()) path.mkdirs()
            } else {
                runOnUI { toast("无内置储存") }
                return@withContext
            }
            saveWordFile = File("${path.absolutePath}/$fileName")
        }
        var out: BufferedWriter? = null
        try {
            out = BufferedWriter(OutputStreamWriter(FileOutputStream(saveWordFile, true)))
            out.write(text)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            try {
                out?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

fun runOnUI(a: () -> Unit) {
    GlobalScope.launch(Dispatchers.Main) { a() }
}

data class CodedChat(var time: Int, var type: Int, var sender: String, var msg: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CodedChat
        if (!msg.contentEquals(other.msg)) return false
        return true
    }

    override fun hashCode(): Int {
        return msg.contentHashCode()
    }
}

data class Chat(var time: Int, var type: Int, var sender: String, var msg: String)

fun Activity.sendToViewHtml(file: File) {
    val viewIntent = Intent(Intent.ACTION_VIEW)
    viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
        viewIntent.setDataAndType(Uri.fromFile(file), "text/html")
    } else {
        val htmlUri = FileProvider.getUriForFile(this, "qhaty.qqex.provider", file)
        viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        viewIntent.setDataAndType(htmlUri, "text/html")
    }
    startActivity(this, viewIntent, null)
}

suspend fun delDB(context: Context) {
    try {
        withContext(Dispatchers.IO) {
            val dir = context.getExternalFilesDir(null)
            val old = File(dir, "slowtable_${mmkv["myQQ", ""]}.db")
            val new = File(dir, "absolutePath}/${mmkv["myQQ", ""]}.db")
            if (new.exists()) new.delete()
            if (old.exists()) old.delete()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        toast("无内置储存 无法读取数据")
    }
}

suspend fun readKey(): String {
    return try {
        withContext(Dispatchers.IO) {
            val dir = application.getExternalFilesDir(null)!!
            val file = File("${dir.absolutePath}/kc")
            if (file.exists()) return@withContext file.readText()
            else ""
        }
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

fun View.gone() {
    GlobalScope.launch(Dispatchers.Main) { visibility = View.GONE }
}

fun View.visable() {
    GlobalScope.launch(Dispatchers.Main) { visibility = View.VISIBLE }
}