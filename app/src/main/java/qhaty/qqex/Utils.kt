package qhaty.qqex

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import com.jaredrummler.android.shell.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*

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

var saveHtmlFile: File? = null
suspend fun appendTextToAppDownload(context: Context, fileName: String, text: String) {
    withContext(Dispatchers.IO) {
        if (saveHtmlFile == null) {
            val path = context.getExternalFilesDir("Save")
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
            val path = context.getExternalFilesDir("Data")
            if (path != null) {
                if (!path.exists()) path.mkdirs()
            } else {
                runOnUI { context.toast("无内置储存") }
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

fun saveWordCloud(context: Context, view: View) {
    GlobalScope.launch(Dispatchers.Default) {
        val bitmap = view.getBitmapCut()
        val date = SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(Date())
        // 保存bitmap
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val galleryPath = File(
                Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_PICTURES
            )
            var fos: FileOutputStream? = null
            withContext(Dispatchers.IO) {
                var file: File? = null
                try {
                    file = File(galleryPath, "QQEX_${Data.friendQQ}${date}.jpg")
                    if (!file.exists()) {
                        file.parentFile!!.mkdirs()
                        file.createNewFile()
                    }
                    fos = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    fos?.close()
                }
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DATA, file!!.absolutePath)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.DESCRIPTION, "保存自: QQEX")
                }
                val uri: Uri? =
                    context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                    )
                val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                intent.data = uri
                context.sendBroadcast(intent)
                withContext(Dispatchers.Main) { context.toast("图片保存成功") }
            }
        } else { //Android Q把文件插入到系统图库
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.TITLE, Data.friendQQ)
                put(MediaStore.Images.Media.DISPLAY_NAME, "QQEX_${Data.friendQQ}${date}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.IS_PENDING, 1)
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }

            val resolver = context.contentResolver
            val collection = MediaStore.Images.Media
                .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val item = resolver.insert(collection, contentValues)

            withContext(Dispatchers.IO) {
                resolver.openFileDescriptor(item!!, "w", null).use { pfd ->
                    val out = FileOutputStream(pfd!!.fileDescriptor)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(item, contentValues, null, null)
                withContext(Dispatchers.Main) { context.toast("图片保存成功") }
            }
        }
    }
}

fun View.getBitmapCut(): Bitmap {
    val v = this
    val bitmap = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val bgDrawable = v.background
    if (bgDrawable != null) {
        bgDrawable.draw(canvas)
    } else {
        canvas.drawColor(Color.WHITE)
    }
    v.draw(canvas)
    return bitmap
}
//
//class SeekListener(
//    private val change: (() -> Unit)? = null,
//    private val stop: (() -> Unit)? = null,
//    private val start: (() -> Unit)? = null
//) : SeekBar.OnSeekBarChangeListener {
//    override fun onStartTrackingTouch(seekBar: SeekBar?) {
//        start?.invoke()
//    }
//
//    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//        change?.invoke()
//    }
//
//    override fun onStopTrackingTouch(seekBar: SeekBar?) {
//        stop?.invoke()
//    }
//}