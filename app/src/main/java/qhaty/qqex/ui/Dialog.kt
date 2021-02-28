package qhaty.qqex.ui

import android.app.Activity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import qhaty.qqex.R
import qhaty.qqex.util.get
import qhaty.qqex.util.mmkv
import qhaty.qqex.util.set
import qhaty.qqex.util.toast

fun Activity.selfKeyDialog() = MaterialDialog(this).show {
    title(R.string.self_key)
    var str = mmkv["self_key", ""]
    input(prefill = str) { _, s -> str = s.toString() }
    positiveButton(R.string.ok) {
        if (str.isBlank()) toast(R.string.not_input_key)
        else mmkv["self_key"] = str
    }
    negativeButton(R.string.cancel)
}