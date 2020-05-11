package qhaty.qqex

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.View
import com.afollestad.assent.Permission
import com.afollestad.assent.askForPermissions
import com.chibatching.kotpref.Kotpref
import com.jaredrummler.android.shell.Shell
import io.noties.markwon.Markwon
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.exp_dialog.view.*
import splitties.alertdialog.alertDialog
import splitties.alertdialog.okButton
import java.lang.reflect.Method


class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Kotpref.init(this)
        Markwon.create(this).setMarkdown(info, infoT)
        qq_mine_edit.setText(Data.meQQ)
        qq_exp_edit.setText(Data.friendQQ)
        key_edit.setText(Data.key)
        val mainContext = this
        exp_bt.setOnClickListener {
            Data.meQQ = qq_mine_edit.text.toString()
            Data.friendQQ = qq_exp_edit.text.toString()
            if (key_edit.text.toString().isNotBlank()) {
                Data.key = key_edit.text.toString()
                if (Data.key.isNotBlank() && Data.meQQ.isNotBlank() && Data.friendQQ.isNotBlank()) {
                    expDialog().show()
                    Ex().startEx(this)
                }
            } else {
                val keyGenText: String = last_chat_edit.text.toString()
                if (keyGenText.toByteArray().size >= 15) {
                    expDialog().show()
                    Ex().startEx(this, keyGenText)
                    return@setOnClickListener
                }

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    alertDialog("未填写key", "请确保授予权限自动获取key\n\n本项目已在GitHub开源,请您放心授予") {
                        okButton {
                            askForPermissions(Permission.READ_PHONE_STATE) {
                                if (it.isAllDenied(Permission.READ_PHONE_STATE)) {
                                    context.toast("请手动授予权限自动获取key")
                                } else {
                                    try {
                                        val tm = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
                                        val method: Method = tm.javaClass.getMethod("getImei")
                                        Data.key = method.invoke(tm) as String
                                        expDialog().show()
                                        Ex().startEx(mainContext, keyGenText)
                                    } catch (e: Exception) {
                                    }
                                }
                            }
                        }
                    }.show()
                } else {
                    toast("自动获取key不适用于Android Q以上,请手动获取")
                }
            }
        }
        set_bt.setOnClickListener {
            val items = arrayOf("群消息导出", "使用root权限")
            alertDialog {
                setMultiChoiceItems(items, booleanArrayOf(false, false)) { _, which, isChecked ->
                    when (which) {
                        0 -> Data.friendOrGroup = !isChecked
                        1 -> {
                            if (isChecked) {
                                if (Shell.SU.available()) Data.hasRoot = true
                            } else Data.hasRoot = false
                        }
                    }
                }
            }.show()
        }
    }

    companion object {
        private val infoT by lazy {
            """# QQ聊天记录导出

## 获取数据库 以下二选一

1. 拥有root权限的手机 授予软件root权限即可
2. 通过系统备份有办法获取到QQ的数据库文件(如MIUI)并进行手动导入(见文档底部)

## 获得key 以下二选一

1. 软件自动获取 (授予读取设备信息权限 Android Q及以上失效 必须用户手动输入)(其实就是IMEI码 手机拨号界面输入*#06#即可获得)
2. 给好友发一条6个汉字或更长的消息（即便消息没有发送成功也可）  
并将这段消息填入计算key的界面中得到key 复制key记录下来  

## 附

### 手动导入

> 将/data/data/com.tencent.mobileqq/databases里面用你的QQ号命名的两个文件复制到sdcard/Android/data/qhaty.qqex/files

例如我的qq号为12345 则这两个文件为:
>12345.db  
slowtable_123456.db  

[github](https://github.com/zsqw123/QQ-Exp-Android)  
        """
        }
    }
}

fun Context.expDialog(): AlertDialog {
    val dialog = alertDialog {
        setCancelable(false)
        val view = View.inflate(this@expDialog, R.layout.exp_dialog, null)
        setView(view)
        ProgressView.progressView = view.progress_bar
        ProgressView.cirProgress = view.cir_progress
        ProgressView.progressText = view.progress_text
    }.apply { setCanceledOnTouchOutside(false) }
    ProgressView.dialog = dialog
    return dialog
}