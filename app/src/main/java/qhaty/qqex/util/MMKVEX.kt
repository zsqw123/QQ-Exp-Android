package qhaty.qqex.util

import com.tencent.mmkv.MMKV

val mmkv = MMKV.defaultMMKV(MMKV.MULTI_PROCESS_MODE, null)!!
inline operator fun <reified T> MMKV.set(key: String, value: T) {
    when (value) {
        is String -> mmkv.encode(key, value)
        is Int -> mmkv.encode(key, value)
        is Boolean -> mmkv.encode(key, value)
        is Float -> mmkv.encode(key, value)
        is Double -> mmkv.encode(key, value)
        is Long -> mmkv.encode(key, value)
        is Set<*> -> mmkv.encode(key, value.map { it.toString() }.toSet())
        else -> mmkv.encode(key, value.toString())
    }
    SPUtil.onKeyChange(key)
}

inline operator fun <reified T> MMKV.get(key: String, defaultValue: T): T {
    return when (defaultValue) {
        is String -> mmkv.decodeString(key, defaultValue) ?: "" as T
        is Int -> mmkv.decodeInt(key, defaultValue)
        is Boolean -> mmkv.decodeBool(key, defaultValue)
        is Float -> mmkv.decodeFloat(key, defaultValue)
        is Double -> mmkv.decodeDouble(key, defaultValue)
        is Long -> mmkv.decodeLong(key, defaultValue)
        is Set<*> -> mmkv.decodeStringSet(key, defaultValue.map { it.toString() }.toSet()) ?: emptySet<String>()
        else -> throw Exception("Type error")
    } as T
}

object SPUtil {
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

    fun onKeyChange(spKey: String) {
        if (spEventList.containsKey(spKey)) spEventList[spKey]?.invoke()
    }
}