package qhaty.qqex.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import qhaty.qqex.databinding.FragSearchBinding

class SearchFragment : BaseFragment() {
    private lateinit var binding: FragSearchBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragSearchBinding.inflate(inflater, container, false)
        return binding.root
    }
}