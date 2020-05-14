package qhaty.qqex

import android.app.AlertDialog
import android.content.Context
import android.telephony.TelephonyManager
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.assent.Permission
import com.afollestad.assent.askForPermissions
import com.jaredrummler.android.shell.Shell
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.exp_dialog.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import qhaty.qqex.MainActivity.Companion.mainActivity
import qhaty.qqex.MainActivity.Companion.mainContext
import splitties.alertdialog.alertDialog
import splitties.alertdialog.negativeButton
import splitties.alertdialog.okButton
import splitties.alertdialog.positiveButton
import java.lang.reflect.Method

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

fun Context.setDialog(): AlertDialog = alertDialog {
    val context = this.context
    val items = arrayOf("群消息导出", "使用root权限")
    setMultiChoiceItems(items, booleanArrayOf(!Data.friendOrGroup, Data.hasRoot)) { _, which, isChecked ->
        when (which) {
            0 -> Data.friendOrGroup = !isChecked
            1 -> {
                if (isChecked) {
                    if (Shell.SU.available()) Data.hasRoot = true
                } else Data.hasRoot = false
            }
        }
    }
}

fun Context.getImeiDialog(): AlertDialog = alertDialog(
    "未填写key", "请确保授予权限自动获取key\n\n本项目已在GitHub开源,请您放心授予"
) {
    okButton {
        mainActivity!!.askForPermissions(Permission.READ_PHONE_STATE) { a ->
            if (a.isAllDenied(Permission.READ_PHONE_STATE)) {
                context.toast("请手动授予权限自动获取key")
            } else {
                try {
                    val tm = context.getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager
                    val method: Method = tm.javaClass.getMethod("getImei")
                    Data.key = method.invoke(tm) as String
                    mainActivity!!.askForPermissions(Permission.WRITE_EXTERNAL_STORAGE) { result ->
                        if (result.isAllGranted(Permission.WRITE_EXTERNAL_STORAGE)) {
                            expDialog().show()
                            Ex().startEx(this@getImeiDialog)
                        }
                    }
                } catch (e: Exception) {
                }
            }
        }
    }
}

fun Context.rootGetKeyDialog(): AlertDialog = alertDialog {
    val items = arrayOf("Key1", "Key2", "Key3")
    setSingleChoiceItems(items, Data.keyType) { _, which ->
        when (which) {
            0 -> Data.keyType = 0
            1 -> Data.keyType = 1
            2 -> Data.keyType = 2
        }
        GlobalScope.launch(Dispatchers.Main) {
            val a = withContext(Dispatchers.Default) {
                try {
                    getKeyUseRoot(mainContext!!)
                    return@withContext 0
                } catch (e: java.lang.Exception) {
                    return@withContext 1
                }
            }
            if (a == 1) {
                toast("获取失败 请检查是否安装QQ")
            } else {
                toast("已获取key")
            }
            mainActivity?.key_edit?.setText(Data.key)
        }
    }
}

fun Context.expWithRebuildDialog(callback: () -> Unit): AlertDialog? {
    return if (checkDBCopied(mainContext!!)) {
        alertDialog("提示", "检测到已导入过聊天数据文件，是否删除重建") {
            positiveButton(R.string.yes) {
                GlobalScope.launch(Dispatchers.Main) {
                    withContext(Dispatchers.IO) { delDB(mainContext!!) }
                    callback.invoke()
                }
            }
            negativeButton(R.string.no) { callback.invoke() }
        }
    } else null
}