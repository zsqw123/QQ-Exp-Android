package qhaty.qqex

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.assent.Permission
import com.afollestad.assent.askForPermissions
import com.chibatching.kotpref.Kotpref
import io.noties.markwon.Markwon
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Kotpref.init(this)
        Markwon.create(this).setMarkdown(info, infoT)

        qq_mine_edit.setText(Data.meQQ)
        qq_exp_edit.setText(Data.friendQQ)
        key_edit.setText(Data.key)
        mainContext = this
        mainActivity = this
        fun startEx() {
            if (key_edit.text.toString().isNotBlank()) {//手动输入Key
                if (Data.meQQ.isNotBlank() && Data.friendQQ.isNotBlank()) {
                    askForPermissions(Permission.WRITE_EXTERNAL_STORAGE) {
                        if (it.isAllGranted(Permission.WRITE_EXTERNAL_STORAGE)) {
                            expDialog().show()
                            Ex().startEx(this)
                        }
                    }
                }
            } else {
                val keyGenText: String = last_chat_edit.text.toString()
                if (keyGenText.toByteArray().size >= 15) {
                    askForPermissions(Permission.WRITE_EXTERNAL_STORAGE) {
                        if (it.isAllGranted(Permission.WRITE_EXTERNAL_STORAGE)) {
                            expDialog().show()
                            Ex().startEx(this, keyGenText)
                        }
                    }
                    return
                }

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) getImeiDialog().show()
                else toast("自动获取key不适用于Android Q以上,请手动获取")
            }
        }
        exp_bt.setOnClickListener {
            Data.meQQ = qq_mine_edit.text.toString()
            Data.friendQQ = qq_exp_edit.text.toString()
            Data.key = key_edit.text.toString()
            val dialog = expWithRebuildDialog { startEx() }
            if (dialog == null) startEx() else dialog.show()
        }
        set_bt.setOnClickListener {
            setDialog().show()
        }
        root_key_bt.setOnClickListener {
            if (!Data.hasRoot) toast("请到设置允许root")
            else rootGetKeyDialog().show()
        }
    }

    companion object {
        var mainContext: Context? = null
        var mainActivity: AppCompatActivity? = null
        private val infoT by lazy {
            """# QQ聊天记录导出

## 获取数据库 以下二选一

1. 拥有root权限的手机 授予软件root权限即可
2. 通过系统备份有办法获取到QQ的数据库文件(如MIUI)并进行手动导入(见文档底部)

## 获得key 以下二选一

1. 软件自动获取
2. 想办法获取到这玩意/data/data/com.tencent.mobileqq/files/kc, 里面的内容就是key (通过系统备份有可能可行)

## 附

### 手动导入

> 将/data/data/com.tencent.mobileqq/databases里面用你的QQ号命名的两个文件复制到sdcard/Android/data/qhaty.qqex/files

例如我的qq号为12345 则这两个文件为:
>12345.db  
slowtable_123456.db
        """
        }
    }
}