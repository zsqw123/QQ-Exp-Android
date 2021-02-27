package qhaty.qqex

import android.app.Application
import com.tencent.mmkv.MMKV

lateinit var application: Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
    }
    init {
        application = this
    }
}