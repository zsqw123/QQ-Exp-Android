package qhaty.qqex.ui

import android.graphics.Color
import github.com.st235.lib_expandablebottombar.ExpandableBottomBar
import github.com.st235.lib_expandablebottombar.ExpandableBottomBarMenuItem
import qhaty.qqex.R

fun ExpandableBottomBar.initQQEXBar() {
    addItems(
        ExpandableBottomBarMenuItem.Builder(context)
            .addItem(R.id.barMain, R.drawable.ic_home, R.string.export, Color.parseColor("#ff75a0"))
            .addItem(R.id.barSearch, R.drawable.ic_search, R.string.search, Color.parseColor("#167096"))
            .addItem(R.id.barSetting, R.drawable.ic_settings, R.string.setting, Color.parseColor("#70af85"))
            .build()
    )

}