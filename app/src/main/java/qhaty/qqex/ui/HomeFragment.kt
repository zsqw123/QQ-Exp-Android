package qhaty.qqex.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import qhaty.qqex.R
import qhaty.qqex.databinding.FragHomeBinding
import qhaty.qqex.method.Ex
import qhaty.qqex.method.copyKeyUseRoot
import qhaty.qqex.util.*

class HomeFragment : BaseFragment() {
    private lateinit var binding: FragHomeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            etMy.setText(mmkv["myQQ", ""])
            etEx.setText(mmkv["exQQ", ""])
            btEx.setOnClickListener {
                val myStr = etMy.text.toString()
                val exStr = etEx.text.toString()
                if (myStr.isBlank() || exStr.isBlank()) {
                    toast(R.string.cannot_blank)
                    return@setOnClickListener
                }
                mmkv["myQQ"] = myStr
                mmkv["exQQ"] = exStr
                val ex = Ex(tvProgress, lifecycleScope, requireActivity())
                val isSelfKey = mmkv["is-self-key", false]
                if (isSelfKey) {
                    val selfKey = mmkv["self-key", ""]
                    if (selfKey.isBlank()) {
                        toast(R.string.self_key_ntfound)
                        return@setOnClickListener
                    } else {
                        mmkv["key"] = selfKey
                        lifecycleScope.launch {
                            tvProgress.visable()
                            btEx.hide()
                            ex.start() {
                                tvProgress.gone()
                                btEx.show()
                            }
                        }
                    }
                } else if (mmkv["root", false]) {
                    lifecycleScope.launch {
                        copyKeyUseRoot()
                        mmkv["key"] = readKey()
                        lifecycleScope.launch {
                            tvProgress.visable()
                            btEx.hide()
                            ex.start() {
                                tvProgress.gone()
                                btEx.show()
                            }
                        }
                    }
                } else {
                    toast(R.string.no_key)
                    return@setOnClickListener
                }
            }
        }
    }
}