package qhaty.qqex

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.core.view.children
import com.jaredrummler.android.shell.Shell
import kotlinx.android.synthetic.main.activity_wordcloud.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.math.abs
import kotlin.math.sqrt


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
        file.writeText(text)
        withContext(Dispatchers.Main) {
            sendToViewHtml(context, file)
            context.toast("文件保存至:Android/data/qhaty.qqex/files/Save")
        }
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

data class Progress(var progress: Int, var msg: String) {
    fun change(m: String) = change(msg = m)
    fun change(progress: Int = this.progress, msg: String = this.msg) {
        this.progress = progress
        this.msg = msg
        Ex.onProgressChange(this)
    }
}


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

fun getKeyUseRoot(context: Context) {
    if (Data.hasRoot) {
        if (Shell.SU.available()) {
            val dir = context.getExternalFilesDir("qqxml")!!
            if (!dir.exists()) dir.mkdirs()
            val qqPkg = "com.tencent.mobileqq"
            val cmd1 =
                "cp -f /data/data/$qqPkg/shared_prefs/appcenter_mobileinfo.xml ${dir.absolutePath}/1.xml"
            val cmd2 =
                "cp -f /data/data/$qqPkg/shared_prefs/DENGTA_META.xml ${dir.absolutePath}/2.xml"
            Shell.SU.run(cmd1, cmd2)
            val regex0 = Regex("""imei">.*?</""")
            val regex1 = Regex("""ress">.*?</""")
            val regex2 = Regex("""IMEI_DENGTA">.*?</""")
            val file1 = File("${dir.absolutePath}/1.xml")
            val file2 = File("${dir.absolutePath}/2.xml")
            when (Data.keyType) {
                1 -> {
                    val text = file1.readText()
                    Data.key = regex0.find(text)!!.value.replace("""imei">""", "").replace("""</""", "")
                }
                0 -> {
                    val text = file1.readText()
                    Data.key = regex1.find(text)!!.value.replace("""ress">""", "").replace("""</""", "")
                }
                2 -> {
                    val text = file2.readText()
                    Data.key = regex2.find(text)!!.value.replace("""IMEI_DENGTA">""", "").replace("""</""", "")
                }
            }
        }
    }
}

fun sendToViewHtml(context: Context, file: File) {
    val viewIntent = Intent(Intent.ACTION_VIEW)
    viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
        viewIntent.setDataAndType(Uri.fromFile(file), "text/html")
    } else {
        val htmlUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
        viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        viewIntent.setDataAndType(htmlUri, "text/html")
    }
    startActivity(context, viewIntent, null)
}

fun checkDBCopied(context: Context): Boolean {
    return try {
        val dir = context.getExternalFilesDir(null)
        File("${dir!!.absolutePath}/${Data.meQQ}.db").exists()
    } catch (e: Exception) {
        context.toast("无内置储存 无法读取数据")
        false
    }
}

suspend fun delDB(context: Context) {
    try {
        withContext(Dispatchers.IO) {
            val dir = context.getExternalFilesDir(null)
            val old = File("${dir!!.absolutePath}/slowtable_${Data.meQQ}.db")
            val new = File("${dir.absolutePath}/${Data.meQQ}.db")
            if (new.exists()) new.delete()
            if (old.exists()) old.delete()
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            context.toast("无内置储存 无法读取数据")
        }
    }
}

fun Context.getStopWords(): List<String> {
    val text = this.readAssetsFileText("stopwords")
    return text.replace("\r\n", "\n").split("\n")
}

fun Context.readAssetsFileText(fileName: String): String {
    val inputStream = this.assets.open(fileName)
    val result = inputStream.use { input ->
        var offset = 0
        var remaining = input.available().also { length ->
            if (length > Int.MAX_VALUE) throw OutOfMemoryError("File $this is too big ($length bytes) to fit in memory.")
        }.toInt()
        val result = ByteArray(remaining)
        while (remaining > 0) {
            val read = input.read(result, offset, remaining)
            if (read < 0) break
            remaining -= read
            offset += read
        }
        if (remaining == 0) result else result.copyOf(offset)
    }
    return result.toString(Charsets.UTF_8)
}

//判定输入的是否是汉字
fun isChinese(c: Char): Boolean {
    val ub = Character.UnicodeBlock.of(c)
    return ub === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub === Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS || ub === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub === Character.UnicodeBlock.GENERAL_PUNCTUATION || ub === Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub === Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
}

//校验String是否全是中文
fun checkStrChinese(name: String): Boolean {
    var res = true
    for (element in name) {
        if (!isChinese(element)) {
            res = false
            break
        }
    }
    return res
}

class SeekListener(
    private val change: (() -> Unit)? = null,
    private val stop: (() -> Unit)? = null,
    private val start: (() -> Unit)? = null
) : SeekBar.OnSeekBarChangeListener {
    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        start?.invoke()
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        change?.invoke()
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        stop?.invoke()
    }
}