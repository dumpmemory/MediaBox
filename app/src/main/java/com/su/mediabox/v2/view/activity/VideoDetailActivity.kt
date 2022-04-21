package com.su.mediabox.v2.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.su.mediabox.R
import com.su.mediabox.config.Api
import com.su.mediabox.databinding.ActivityAnimeDetailBinding
import com.su.mediabox.pluginapi.v2.action.DetailAction
import com.su.mediabox.pluginapi.v2.action.PlayAction
import com.su.mediabox.util.Util.setTransparentStatusBar
import com.su.mediabox.util.showToast
import com.su.mediabox.view.fragment.ShareDialogFragment
import com.su.mediabox.util.coil.CoilUtil.loadGaussianBlurCover
import com.su.mediabox.util.getAction
import com.su.mediabox.util.getActionIns
import com.su.mediabox.util.putAction
import com.su.mediabox.v2.viewmodel.VideoDetailViewModel
import com.su.mediabox.view.activity.BasePluginActivity
import com.su.mediabox.view.adapter.type.dynamicGrid
import com.su.mediabox.view.adapter.type.initTypeList
import com.su.mediabox.view.adapter.type.typeAdapter

class VideoDetailActivity : BasePluginActivity<ActivityAnimeDetailBinding>() {

    private val viewModel by viewModels<VideoDetailViewModel>()
    override var statusBarSkin: Boolean = false
    private var isClick = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTransparentStatusBar(window, isDark = false)

        //TODO 暂时兼容处理
        viewModel.partUrl = intent.getStringExtra("partUrl") ?: ""
        getAction<DetailAction>()?.also {
            viewModel.partUrl = it.url
        }

        //详情数据列表
        mBinding.rvAnimeDetailActivityInfo.dynamicGrid().initTypeList { }

        //下拉刷新
        mBinding.srlAnimeDetailActivity.setOnRefreshListener {

            viewModel.getAnimeDetailData()
        }

        mBinding.atbAnimeDetailActivityToolbar.run {
            setBackButtonClickListener { finish() }
            // 分享
            setButtonEnable(0, false)
            setButtonClickListener(0) {
                ShareDialogFragment().setShareContent(Api.MAIN_URL + viewModel.partUrl)
                    .show(supportFragmentManager, "share_dialog")
            }
            addButton(null)
            // 收藏
            setButtonEnable(1, false)
            setButtonClickListener(1) {
                isClick = true
                viewModel.switchFavState()
            }

            //收藏状态
            viewModel.isFavVideo.observe(this@VideoDetailActivity) {
                if (it) {
                    setButtonDrawable(1, R.drawable.ic_star_white_24_skin)
                    if (isClick)
                        getString(R.string.favorite_succeed).showToast()
                } else {
                    setButtonDrawable(1, R.drawable.ic_star_border_white_24)
                    if (isClick)
                        getString(R.string.remove_favorite_succeed).showToast()
                }
                isClick = false
            }

        }

        //详情数据
        viewModel.videoData.observe(this, Observer {
            mBinding.srlAnimeDetailActivity.isRefreshing = false
            mBinding.atbAnimeDetailActivityToolbar.apply {
                setButtonEnable(0, true)
                setButtonEnable(1, true)
            }

            if (viewModel.cover.isBlank()) return@Observer
            //高斯模糊封面背景
            mBinding.ivAnimeDetailActivityBackground.loadGaussianBlurCover(viewModel.cover, this)
            //标题
            mBinding.atbAnimeDetailActivityToolbar.titleText = viewModel.title
            //详情数据
            mBinding.rvAnimeDetailActivityInfo.typeAdapter().submitList(it.second)

            //嵌入当前视频名称
            mBinding.rvAnimeDetailActivityInfo.typeAdapter().setTag(viewModel.title)
        })

        mBinding.srlAnimeDetailActivity.isRefreshing = true
        viewModel.getAnimeDetailData()
    }

    override fun getBinding(): ActivityAnimeDetailBinding =
        ActivityAnimeDetailBinding.inflate(layoutInflater)

    @SuppressLint("NotifyDataSetChanged")
    override fun onChangeSkin() {
        super.onChangeSkin()
        mBinding.rvAnimeDetailActivityInfo.typeAdapter().notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mBinding.rvAnimeDetailActivityInfo.typeAdapter().notifyDataSetChanged()
    }

    override fun startActivity(intent: Intent?, options: Bundle?) {
        //主动向下一级路由目标提供一些信息
        intent?.apply {
            getActionIns<PlayAction>()?.apply {
                coverUrl = viewModel.cover
                detailPartUrl = viewModel.partUrl
                videoName = viewModel.title
                putAction(this)
            }
        }
        super.startActivity(intent, options)
    }
}
