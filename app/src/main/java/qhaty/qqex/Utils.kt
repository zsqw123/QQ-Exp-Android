package qhaty.qqex

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
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


/**
 * 对view实现拖拽移动、双指缩放效果（默认全开启）
 * 使用方法：1创建DragTouchListener实例 ；2设置监听 view.setOnTouchListener(DragTouchListener);
 * Created by alan on 2019/1/3 0007.
 */
class DragTouchListener @JvmOverloads constructor(limitParent: ViewGroup? = null) : OnTouchListener {
    private val dm: DisplayMetrics? = null
    private var maxWidth = 0
    private var maxHeight = 0
    private var lastX = 0
    private var lastY = 0

    //刚触摸时的view坐标（用来获取按下时view的大小）
    private var oriLeft = 0
    private var oriRight = 0
    private var oriTop = 0
    private var oriBottom = 0
    private var baseValue = 0f
    private var dragListener: DragListener
    var originalScale = 0f

    /**
     * 当前触摸模式：
     * 无触摸；
     * 单指触摸；
     * 双指触摸；
     */
    private var currentTouchMode = TOUCH_NONE

    /**
     * 是否开启：双指触摸缩放
     */
    private var touchTwoZoomEnable = true

    /**
     * 是否取消：触摸移动
     */
    private var isCancleTouchDrag = false

    /**
     * 产生效果的view（缩放、拖拽效果）
     */
    private val mEffectView: View? = null

    /**
     * 控制是否开启两指触摸缩放
     * @param touchTwoZoomEnable
     */
    fun setTouchTwoZoomEnable(touchTwoZoomEnable: Boolean): DragTouchListener {
        this.touchTwoZoomEnable = touchTwoZoomEnable
        return this
    }

    /**
     * 设置：是否取消拖拽移动
     * @param cancleTouchDrag
     */
    fun setCancleTouchDrag(cancleTouchDrag: Boolean): DragTouchListener {
        isCancleTouchDrag = cancleTouchDrag
        return this
    }

    interface DragListener {
        fun actionDown(v: View?)
        fun actionUp(v: View?)
        fun dragging(listenerView: View?, left: Int, top: Int, right: Int, bottom: Int)
        fun zooming(scale: Float)
    }

    constructor(limitParent: ViewGroup?, dragListener: DragListener) : this(limitParent) {
//        maxHeight = viewGroup.getHeight();
//        maxWidth = viewGroup.getWidth();
        this.dragListener = dragListener
    }

    private var moveFlag = false
    override fun onTouch(v: View, event: MotionEvent): Boolean {
//        int action = event.getAction();
        if (event.action == MotionEvent.ACTION_UP) v.performClick()
        val action = event.action and MotionEvent.ACTION_MASK
        //屏蔽父控件拦截onTouch事件
        v.parent.requestDisallowInterceptTouchEvent(true)
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                dragListener.actionDown(v)
                lastX = event.rawX.toInt()
                lastY = event.rawY.toInt()
                oriLeft = v.left
                oriRight = v.right
                oriTop = v.top
                oriBottom = v.bottom
                currentTouchMode = TOUCH_ONE
                baseValue = 0f
                lastScale = 1f
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                oriLeft = v.left
                oriRight = v.right
                oriTop = v.top
                oriBottom = v.bottom
                currentTouchMode = TOUCH_TWO
                baseValue = 0f
                lastScale = 1f
            }
            MotionEvent.ACTION_MOVE -> {
                moveFlag = !moveFlag
                if (event.pointerCount == 2) {
                    if (touchTwoZoomEnable) {
                        val x = event.getX(0) - event.getX(1)
                        val y = event.getY(0) - event.getY(1)
                        val value = sqrt(x * x + y * y.toDouble()).toFloat() // 计算两点的距离
                        if (baseValue == 0f) {
                            baseValue = value
                        } else {
                            if (value - baseValue >= 10 || value - baseValue <= -10) {
                                // 当前两点间的距离 除以 手指落下时两点间的距离就是需要缩放的比例。
                                val scale = value / baseValue
                                //                                Log.i("TAG", "onTouch-scale: "+scale+"  value: "+value+"  ; baseValue: "+baseValue);
                                //缩放view(不能用当前touch方法里的view，会造成频闪效果)（只能在其他view调用）
//                                    mEffectView.setScaleX(scale);
//                                    mEffectView.setScaleY(scale);

                                //改变大小进行缩放（只能缩放当前view的大小，如果是父布局，则里面的子控件无法缩小）
                                touchZoom(v, scale)
                                dragListener.zooming(scale)
                            }
                        }
                    }
                } else if (currentTouchMode == TOUCH_ONE) { //1个手指
                    //如果取消拖拽，触摸就交给系统处理
                    if (isCancleTouchDrag) {
                        return false
                    }
                    //移动图片位置
                    touchDrag(v, event)
                }
            }
            MotionEvent.ACTION_UP -> {
                baseValue = 0f
                dragListener.actionUp(v)
            }
            else -> currentTouchMode = TOUCH_NONE
        }
        return true
    }

    private var lastScale = 1f

    /**
     * 缩放view
     * @param v
     * @param scale  当前距离按下时的比例  (0.8：缩小到0.8倍)
     */
    private fun touchZoom(v: View, scale: Float) {
        val oriWidth = abs(oriRight - oriLeft)
        val oriHeight = abs(oriBottom - oriTop)

//        if(lastScale == 0)lastScale = scale;
        //需要缩放的比例（1-0.9=0.1，需要缩小0.1倍；-0.1：放大0.1倍）
        val zoomScale = lastScale - scale
        val dx = (oriWidth * zoomScale / 2f).toInt()
        val dy = (oriHeight * zoomScale / 2f).toInt()
        val left = v.left + dx
        val top = v.top + dy
        val right = v.right - dx
        val bottom = v.bottom - dy
        v.layout(left, top, right, bottom)
        lastScale = scale
    }

    private fun touchDrag(v: View, event: MotionEvent) {
        val dx = event.rawX.toInt() - lastX
        val dy = event.rawY.toInt() - lastY
        var left = v.left + dx
        var top = v.top + dy
        var right = v.right + dx
        var bottom = v.bottom + dy
        if (maxWidth != 0 && maxHeight != 0) {
            //防止移出屏幕
            if (left < 0) {
                left = 0
                right = left + v.width
            }
            if (right > maxWidth) {
                right = maxWidth
                left = right - v.width
            }
            if (top < 0) {
                top = 0
                bottom = top + v.height
            }
            if (bottom > maxHeight) {
                bottom = maxHeight
                top = bottom - v.height
            }
        }
        v.layout(left, top, right, bottom)
        dragListener.dragging(v, left, top, right, bottom)
        lastX = event.rawX.toInt()
        lastY = event.rawY.toInt()
    }

    companion object {
        private const val TOUCH_NONE = 0x00
        private const val TOUCH_ONE = 0x20
        private const val TOUCH_TWO = 0x21
    }

    /**
     *
     * @param limitParent 拖动限制区域，防止移出屏幕(null:拖动无限制)
     */
    init {
        if (limitParent != null) {
            val vto = limitParent.viewTreeObserver
            vto.addOnPreDrawListener {
                maxHeight = limitParent.measuredHeight
                maxWidth = limitParent.measuredWidth
                //                Log.i("TAG", "maxHeight: "+maxHeight+", maxWidth"+maxWidth);
                true
            }
        }
        dragListener = object : DragListener {
            override fun actionDown(v: View?) {}
            override fun actionUp(v: View?) {}
            override fun dragging(
                listenerView: View?,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int
            ) {
            }

            override fun zooming(scale: Float) {}
        }
    }
}