package qhaty.qqex.util

import com.tencent.mmkv.MMKV

private val mmkv = MMKV.defaultMMKV(MMKV.MULTI_PROCESS_MODE, null)!!
inline operator fun <reified T> MMKV.set(key: String, value: T) {
    sPut(key, value)
}

/**
 *
 * @receiver Context
 * @param key String 键值对名
 * @param obj T 默认值
 * @return T 返回值，与 obj 类型相同
 */
inline fun <reified T> sPut(key: String, obj: T): T {
    SPUtil.put(key, obj as Any)
    return obj
}

inline fun <reified T> sGet(key: String, obj: T) = SPUtil.get(key, obj as Any) as T
fun sRemove(key: String) = mmkv.remove(key)

//fun Context.sClear(spName: String = SPUtil.FILE_NAME) = SPUtil.clear(this, spName)

object SPUtil {
    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     */
    fun put(key: String, obj: Any) {
        when (obj) {
            is String -> mmkv.encode(key, obj)
            is Int -> mmkv.encode(key, obj)
            is Boolean -> mmkv.encode(key, obj)
            is Float -> mmkv.encode(key, obj)
            is Long -> mmkv.encode(key, obj)
            is Set<*> -> mmkv.encode(key, obj.map { it.toString() }.toSet())
            else -> mmkv.encode(key, obj.toString())
        }
        onKeyChange(key)
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     */
    fun get(key: String, defaultObject: Any): Any {
        return when (defaultObject) {
            is String -> mmkv.decodeString(key, defaultObject)
            is Int -> mmkv.decodeInt(key, defaultObject)
            is Boolean -> mmkv.decodeBool(key, defaultObject)
            is Float -> mmkv.decodeFloat(key, defaultObject)
            is Long -> mmkv.decodeLong(key, defaultObject)
            is Set<*> -> mmkv.decodeStringSet(key, defaultObject.map { it.toString() }.toSet())
            else -> defaultObject
        }!!
    }

    /**
     * 查询某个key是否已经存在
     */
    fun contains(key: String?): Boolean = mmkv.contains(key)

    private val spEventList = hashMapOf<String, () -> Unit>()

    /**
     * 设置 sp 监听
     * @param spKey String
     * @param event 响应事件
     */
    fun registEvent(spKey: String, event: () -> Unit) {
        spEventList[spKey] = event
    }

    /**
     * 删除 sp 监听响应事件
     * @param spKey String
     */
    fun unregistEvent(spKey: String) {
        kotlin.runCatching { spEventList.remove(spKey) }
    }

    private fun onKeyChange(spKey: String) {
        if (spEventList.containsKey(spKey)) spEventList[spKey]?.invoke()
    }
}