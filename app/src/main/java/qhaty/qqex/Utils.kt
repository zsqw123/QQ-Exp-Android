package qhaty.qqex

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


fun Context.toast(str: String? = null, id: Int? = null) {
    Toast.makeText(this, str ?: this.resources.getText(id!!), Toast.LENGTH_SHORT).show()
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

suspend fun textToAppDownload(context: Context, fileName: String, text: String) {
    withContext(Dispatchers.IO) {
        val path = context.getExternalFilesDir("Save")
        val file = File("$path/$fileName.html")
        if (file.exists()) file.delete()
        file.createNewFile()
        FileOutputStream(file).write(text.toByteArray())
    }

}

suspend fun textToAppData(context: Context, fileName: String, text: String) {
    withContext(Dispatchers.IO) {
        val path = context.getExternalFilesDir("Data")
        if (path != null) {
            if (!path.exists()) path.mkdirs()
        } else {
            runOnUI { context.toast("无内置储存") }
            return@withContext
        }
        val file = File("${path.absolutePath}/$fileName")
        if (file.exists()) file.delete()
        file.createNewFile()
        file.writeText(text)
    }
}

fun runOnUI(a: () -> Unit) {
    GlobalScope.launch(Dispatchers.Main) { a() }
}

data class Progress(var progress: Int, var msg: String)

class ProgressView {
    companion object {
        var progressView: ProgressBar? = null
        var progressText: TextView? = null
        var dialog: AlertDialog? = null
        var cirProgress: ProgressBar? = null
    }
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