package qhaty.qqex.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import qhaty.qqex.databinding.FragHomeBinding
import qhaty.qqex.util.sGet

class HomeFragment : BaseFragment() {
    private lateinit var binding: FragHomeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            etMy.setText(sGet("myQQ", ""))
            etEx.setText(sGet("exQQ", ""))
        }
    }
}