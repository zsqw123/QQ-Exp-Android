package qhaty.qqex

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.*
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

fun textToDownload(context: Context, fileName: String, text: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/QQEX")
        val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            val outputStream = context.contentResolver.openOutputStream(uri)
            outputStream?.write(text.toByteArray())
        }
    } else {
        @Suppress("DEPRECATION")
        val path = "${Environment.getExternalStorageDirectory().absolutePath}/${Environment.DIRECTORY_DOWNLOADS}/QQEX"
        if (!File(path).exists()) File(path).mkdirs()
        val file = File("$path/$fileName")
        if (file.exists()) file.delete()
        file.createNewFile()
        FileOutputStream(file).write(text.toByteArray())
    }
}