package qhaty.qqex

import android.app.Activity
import android.os.Bundle
import com.chibatching.kotpref.Kotpref
import com.jaredrummler.android.shell.Shell
import io.noties.markwon.Markwon
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Kotpref.init(this)
        Markwon.create(this).setMarkdown(info, infoT)
        exp_bt.setOnClickListener { GlobalScope.launch { } }
    }

    companion object {
        private val infoT by lazy {
            """# QQ聊天记录导出

## 获取数据库 以下二选一

1. 拥有root权限的手机 授予软件root权限即可
2. 通过系统备份有办法获取到QQ的数据库文件(如MIUI)并进行手动导入(见文档底部)

## 获得key 以下二选一

1. 软件自动获取 (授予读取设备信息权限)
2. 给好友发一条七个汉字或更长的消息（即便消息没有发送成功也可）  
并将这段消息填入计算key的界面中得到key 复制key记录下来  

## 附

### 手动导入

> 将/data/data/com.tencent.mobileqq/databases里面用你的QQ号命名的两个文件复制到sdcard/qhaty.qqex/files

例如我的qq号为12345 则这两个文件为:
>12345.db  
slowtable_123456.db
        """
        }
    }
}
