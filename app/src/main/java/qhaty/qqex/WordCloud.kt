package qhaty.qqex
//
//import android.content.Context
//import android.graphics.Color
//import android.graphics.Point
//import android.graphics.Rect
//import android.util.AttributeSet
//import android.util.Log
//import android.view.View
//import android.view.ViewGroup
//import android.widget.FrameLayout
//import android.widget.TextView
//import java.util.*
//import kotlin.math.cos
//import kotlin.math.sin
//
///**
// * Created by chao on 2019/2/15.
// */
//class WordCloudView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
//    FrameLayout(context, attrs, defStyleAttr), View.OnClickListener {
//    var random = Random()
//    var words = arrayListOf<String>()
//    var placed = HashSet<View>()
//    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
//        for (i in 0 until childCount) {
//            val v = getChildAt(i)
//            if (placed.contains(v)) continue
//            val w = v.measuredWidth
//            val h = v.measuredHeight
//            var pivotX = width / 3 + random.nextInt(width / 3)
//            var pivotY = height / 3 + random.nextInt(height / 3)
//            val spiral = generateSpiral()
//            for (p in spiral) {
//                pivotX += p.x
//                pivotY += p.y
//                Log.d("chao", "place $pivotX,$pivotY")
//                val r1 = getVisualRect(pivotX, pivotY, w, h, v.rotation)
//                var isOverlap = false
//                for (pv in placed) {
//                    val r2 = getVisualRect(pv)
//                    if (isOverlap(r1, r2)) {
//                        isOverlap = true
//                        break
//                    }
//                }
//                if (!isOverlap) {
//                    Log.d("chao", "placed")
//                    val r = getRect(pivotX, pivotY, w, h)
//                    v.layout(r.left, r.top, r.right, r.bottom)
//                    break
//                }
//            }
//            placed.add(v)
//        }
//    }
//
//    fun getRect(pivotX: Int, pivotY: Int, width: Int, height: Int) =
//        Rect(pivotX - width / 2, pivotY - height / 2, pivotX + width / 2, pivotY + height / 2)
//
//    fun getVisualRect(pivotX: Int, pivotY: Int, width: Int, height: Int, rotation: Float) =
//        if (rotation != 0f) getRect(pivotX, pivotY, height, width) else getRect(pivotX, pivotY, width, height)
//
//
//    fun getVisualRect(v: View): Rect = getVisualRect(
//        (v.right + v.left) / 2,
//        (v.bottom + v.top) / 2,
//        v.measuredWidth,
//        v.measuredHeight,
//        v.rotation
//    )
//
//    //    public void setWords(String[] words) {
//    //        this.words = words;
//    //        placed.clear();
//    //        removeAllViews();
//    //        for(final String word : words) {
//    //            addTextView(word);
//    //        }
//    //    }
//    var params = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//    var rotates = floatArrayOf(0f, 90f, 270f)
//
//    fun addTextView(word: String?, weight: Int) {
//        val tv = TextView(context)
//        tv.text = word
//        tv.textSize = weight.toFloat()
//        tv.rotation = rotates[random.nextInt(rotates.size)]
//        tv.setOnClickListener(this)
//        addView(tv, params)
//    }
//
//    var lastText: TextView? = null
//    override fun onClick(v: View) {
//        if (v is TextView) {
//            Log.e("chao", "click " + v.text)
//            v.setTextColor(Color.RED)
//            if (lastText != null) {
//                lastText!!.setTextColor(Color.BLACK)
//            }
//            lastText = v
//        }
//    }
//
//    private fun generateSpiral(): List<Point> {
//        val res: MutableList<Point> = ArrayList()
//        var a = 10
//        val w = 5
//        val sita = Math.PI
//        var t = 0.0
//        while (t < 10 * Math.PI) {
//            val x = java.lang.Double.valueOf(a * cos(w * t + sita)).toInt()
//            val y = java.lang.Double.valueOf(a * sin(w * t + sita)).toInt()
//            a += 1
//            res.add(Point(x, y))
//            Log.e("chao", "$x, $y")
//            t += 0.1
//        }
//        return res
//    }
//
//    companion object {
//        fun isOverlap(r1: Rect, r2: Rect): Boolean =
//            r1.right >= r2.left && r2.right >= r1.left && r1.bottom >= r2.top && r2.bottom >= r1.top
//    }
//}