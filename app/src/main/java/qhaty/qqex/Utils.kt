package qhaty.qqex

import android.content.Context
import android.widget.Toast

fun Context.toast(str: String? = null, id: Int? = null) {
    Toast.makeText(this, str ?: this.resources.getText(id!!), Toast.LENGTH_SHORT).show()
}