package com.su.mediabox.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.su.mediabox.R
import com.su.mediabox.bean.ResponseDataType
import com.su.mediabox.databinding.FragmentRankBinding
import com.su.mediabox.util.showToast
import com.su.mediabox.util.smartNotifyDataSetChanged
import com.su.mediabox.view.adapter.AnimeShowAdapter
import com.su.mediabox.view.adapter.decoration.AnimeShowItemDecoration
import com.su.mediabox.view.adapter.spansize.AnimeShowSpanSize
import com.su.mediabox.viewmodel.RankListViewModel

@Deprecated("更新2.0后删除")
class RankFragment : BaseFragment<FragmentRankBinding>() {
    private var partUrl: String = ""
    private lateinit var viewModel: RankListViewModel
    private lateinit var adapter: AnimeShowAdapter.GridRecyclerView1Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(RankListViewModel::class.java)
        val arguments = arguments

        try {
            partUrl = arguments?.getString("partUrl") ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            e.message?.showToast(Toast.LENGTH_LONG)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        adapter = AnimeShowAdapter.GridRecyclerView1Adapter(requireActivity(), viewModel.rankList)
        adapter.showRankNumber = true
        mBinding.run {
            rvRankFragment.layoutManager = GridLayoutManager(activity, 4)
                .apply {
                    spanSizeLookup = AnimeShowSpanSize(adapter)
                }
            rvRankFragment.addItemDecoration(AnimeShowItemDecoration())
            rvRankFragment.setHasFixedSize(true)
            rvRankFragment.adapter = adapter
            srlRankFragment.setOnRefreshListener {
                viewModel.getRankListData(partUrl)
            }
            srlRankFragment.setOnLoadMoreListener {
                viewModel.pageNumberBean?.let {
                    viewModel.getRankListData(it.actionUrl, isRefresh = false)
                    return@setOnLoadMoreListener
                }
                mBinding.srlRankFragment.finishLoadMore()
                getString(R.string.no_more_info).showToast()
            }
        }

        viewModel.mldRankData.observe(viewLifecycleOwner, Observer {
            mBinding.srlRankFragment.closeHeaderOrFooter()
            adapter.smartNotifyDataSetChanged(it.first, it.second, viewModel.rankList)

            when (it.first) {
                ResponseDataType.REFRESH, ResponseDataType.LOAD_MORE -> hideLoadFailedTip()
                ResponseDataType.FAILED -> {
                    showLoadFailedTip(getString(R.string.load_data_failed_click_to_retry)) {
                        viewModel.getRankListData(partUrl)
                        hideLoadFailedTip()
                    }
                }
            }
        })

        mBinding.srlRankFragment.autoRefresh()
    }

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRankBinding =
        FragmentRankBinding.inflate(inflater, container, false)

}