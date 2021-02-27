package qhaty.qqex

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import qhaty.qqex.databinding.ActMainBinding
import qhaty.qqex.ui.HomeFragment
import qhaty.qqex.ui.SearchFragment
import qhaty.qqex.ui.SettingFragment
import qhaty.qqex.ui.initQQEXBar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setBar()
        binding.run {
            bar.initQQEXBar()
            bar.onItemSelectedListener = { _, item ->
                val idParse = mapOf(R.id.barMain to 0, R.id.barSearch to 1, R.id.barSetting to 2)
                pager.currentItem = idParse[item.itemId]!!
            }
            pager.isUserInputEnabled = false
            pager.adapter = object : FragmentStateAdapter(this@MainActivity) {
                override fun getItemCount(): Int = fragments.size
                override fun createFragment(position: Int): Fragment = fragments[position]
                val fragments = arrayOf<Fragment>(
                    HomeFragment(), SearchFragment(), SettingFragment()
                )
            }
        }
    }

    private fun setBar() {
        window.statusBarColor = 0
        window.navigationBarColor = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView: View = window.decorView
            @Suppress("DEPRECATION")
            decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}