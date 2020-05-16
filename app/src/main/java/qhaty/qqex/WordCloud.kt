package qhaty.qqex

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import jackmego.com.jieba_android.JiebaSegmenter
import kotlinx.android.synthetic.main.activity_wordcloud.*
import kotlinx.android.synthetic.main.activity_wordcloud.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import kotlin.math.cos
import kotlin.math.log
import kotlin.math.sin

class WordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wordcloud)
        var r = 0
        var g = 0
        var b = 0

        class Seek(val value: Int) : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                when (value) {
                    1 -> r = progress
                    2 -> g = progress
                    3 -> b = progress
                }
                color_preview.setBackgroundColor(Color.rgb(r, g, b))
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                for (i in word_cloud_view.children) {
                    if (i is TextView) {
                        i.setTextColor(Color.rgb(r, g, b))
                    }
                }
            }
        }
        color_seek_r.setOnSeekBarChangeListener(Seek(1))
        color_seek_g.setOnSeekBarChangeListener(Seek(2))
        color_seek_b.setOnSeekBarChangeListener(Seek(3))
        save_bt.setOnClickListener { saveWordCloud(this, word_cloud_view) }
        val path = getExternalFilesDir("Data")
        if (path != null) {
            if (!path.exists()) path.mkdirs()
            val file = File("${path.absolutePath}/${Data.friendQQ}")
            if (file.exists()) {
                val chatText = file.readText()
                val wordList = mutableListOf<Word>()
                val copy = mutableListOf<Word>()
                val result = arrayListOf<String>()
                word_cloud_view_progress?.visibility = View.VISIBLE
                GlobalScope.launch {
                    withContext(Dispatchers.Default) {
                        JiebaSegmenter.getJiebaSegmenterSingleton().process(chatText, JiebaSegmenter.SegMode.INDEX)
                            .forEach { result += it.word }
                    }
                    val stopWords: List<String> = getStopWords()
                    withContext(Dispatchers.Default) {
                        for (it in result) {
                            if (!checkStrChinese(it)) continue
                            var jump = false
                            for (stopWord in stopWords) {
                                if (it == stopWord) {
                                    jump = true
                                    break
                                }
                            }
                            if (jump) continue
                            var parse = false
                            for (i in wordList) {
                                if (i.str == it) {
                                    i.weight = i.weight + 1
                                    parse = true
                                    break
                                }
                            }
                            if (!parse) wordList += Word(it)
                        }
                        wordList.sortByDescending { it.weight }
                        for (i in wordList.indices) {
                            copy += wordList[i]
                            if (i > 300) break
                        }
                    }
                    withContext(Dispatchers.Main) {
                        copy.forEach {
                            word_cloud_view.addTextView(it.str, it.weight)
                        }
                        word_cloud_view_progress?.visibility = View.GONE
                    }
                }
            }
        } else {
            toast("无内置储存")
        }
    }
}

//参考 https://github.com/rome753/WordCloudView
class WordCloudView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr), View.OnClickListener {
    private var random = Random()
    private var placed = HashSet<View>()
    private var words = mutableListOf<Word>()
    private val spiralPoints = genSpiral()
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        for (i in 0 until childCount) {
            val v = getChildAt(i)
            if (placed.contains(v)) continue
            val w = v.measuredWidth
            val h = v.measuredHeight
            var centerX = width / 3 + random.nextInt(width / 3)
            var centerY = height / 3 + random.nextInt(height / 3)
            for (p in spiralPoints) {
                centerX += p.x
                centerY += p.y
                val r1 = getVisualRect(centerX, centerY, w, h, v.rotation)
                var isOverlap = false
                for (pv in placed) {
                    val r2 = getVisualRect(pv)
                    if (isOverlap(r1, r2)) {
                        isOverlap = true
                        break
                    }
                }
                if (!isOverlap) {
                    val r = getRect(centerX, centerY, w, h)
                    v.layout(r.left, r.top, r.right, r.bottom)
                    break
                }
            }
            placed.add(v)
        }
    }

    private fun getRect(pivotX: Int, pivotY: Int, width: Int, height: Int) =
        Rect(pivotX - width / 2, pivotY - height / 2, pivotX + width / 2, pivotY + height / 2)

    private fun getVisualRect(pivotX: Int, pivotY: Int, width: Int, height: Int, rotation: Float) =
        if (rotation != 0f) getRect(pivotX, pivotY, height, width) else getRect(pivotX, pivotY, width, height)

    private fun getVisualRect(v: View): Rect = getVisualRect(
        (v.right + v.left) / 2,
        (v.bottom + v.top) / 2,
        v.measuredWidth,
        v.measuredHeight,
        v.rotation
    )

    private var params = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    private var rotates = floatArrayOf(0f, 90f, 270f)

    fun addTextView(word: String, weight: Int, scale: Int = 1, logFloat: Float = 1.6f) {
        val tv = TextView(context)
        tv.text = word
        words.add(Word(word, weight))
        tv.textSize = 3F * scale * log(weight.toFloat(), logFloat)
        tv.rotation = rotates[random.nextInt(rotates.size)]
        tv.setOnClickListener(this)
        addView(tv, params)
    }

    fun scale(scale: Float) {
        word_cloud_view_progress?.visibility = View.VISIBLE
        removeAllViews()
        for (word in words) {
            val tv = TextView(context)
            tv.text = word.str
            tv.textSize = 3F * scale * log(word.weight.toFloat(), 1.6f)
            tv.rotation = rotates[random.nextInt(rotates.size)]
            tv.setOnClickListener(this)
            addView(tv, params)
        }
        word_cloud_view_progress?.visibility = View.GONE
    }

    override fun onClick(v: View) {
        if (v is TextView) {
            for (word in words) {
                if (v.text == word.str) {
                    context?.toast(word.str + ":" + word.weight + "次")
                }
            }
        }
    }

    private fun genSpiral(): List<Point> {
        val res: MutableList<Point> = ArrayList()
        var v = 10 //线速度控制了大小，越大走得越快螺线形状越大
        val w = 5 //角速度控制了疏密，越小越稀疏，单位时间内旋转少
        var t = 0.0
        while (t < 10 * Math.PI) {
            val x = (v * t * cos(w * t)).toInt()
            val y = (v * t * sin(w * t)).toInt()
            v += 1
            res.add(Point(x, y))
            t += 0.1
        }
        return res
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY)
        )
    }

    companion object {
        fun isOverlap(r1: Rect, r2: Rect): Boolean =
            r1.right >= r2.left && r2.right >= r1.left && r1.bottom >= r2.top && r2.bottom >= r1.top
    }
}

class CloudViewGroup @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY)
        )
    }
}