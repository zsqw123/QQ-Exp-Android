package qhaty.qqex

import java.io.File
import java.io.IOException

class Ex {
    companion object {
        suspend fun doEx(libFileNew: File, libFileOld: File): File? {
            return null
        }
    }
}

@Throws(IOException::class)
fun initFile(path: String) {
    val file = File(path)
    if (!file.parentFile?.exists()!!) {
        file.parentFile?.mkdirs()
    }
    val imgFiles = File(file.parentFile!!.absolutePath+"/images")
    if (!imgFiles.exists()) {
        imgFiles.mkdirs()
    }
    if (file.exists()) {
        file.delete()
    }
    file.createNewFile()
}