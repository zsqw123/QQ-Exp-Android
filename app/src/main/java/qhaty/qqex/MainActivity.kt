package qhaty.qqex

import android.os.Bundle
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
        binding.bar.initQQEXBar()
        binding.pager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
            val fragments = arrayOf<Fragment>(
                HomeFragment(), SearchFragment(), SettingFragment()
            )
        }
    }
}