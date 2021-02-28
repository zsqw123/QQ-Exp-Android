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

operator fun MMKV.get(key: String, event: () -> Unit) {
    SPUtil.registEvent(key, event)
}

/**
 * 注册 mmkv key 响应事件, 若 event 为 null 则删除响应事件.
 * @param key String
 * @param event 响应事件
 */
fun mmkv(key: String, event: (() -> Unit)?) {
    if (event == null) SPUtil.unregistEvent(key)
    else SPUtil.registEvent(key, event)
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
     * @return Boolean 是否执行成功
     */
    fun unregistEvent(spKey: String): Boolean {
        return try {
            spEventList.remove(spKey)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * sp 监听响应事件
     * @param spKey String
     * @return Boolean 是否执行成功
     */
    fun onKeyChange(spKey: String): Boolean {
        return try {
            if (spEventList.containsKey(spKey)) spEventList[spKey]?.invoke()
            true
        } catch (e: Exception) {
            false
        }
    }
}

//fun mmkvTest() {
//    mmkv("test") { // 注册 pref 监听
//        println("test-------test-change")
//    }
//    println("test-------" + mmkv["test", false].toString()) // 获取pref, 第二个参数为默认值
//    mmkv["test"] = true // 修改 pref
//    println("test-------" + mmkv["test", false].toString()) // 获取pref, 第二个参数为默认值
//    mmkv("test", null) // 取消监听
//}