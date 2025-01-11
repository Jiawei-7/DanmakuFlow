package com.wjw.danma

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.wjw.danma.database.DanMuRepository
import com.wjw.danma.util.InputDialog

class MainActivity : AppCompatActivity() {
    private lateinit var mDanMuView: DanMuView
    private lateinit var mStartOrStopButton: Button
    private lateinit var danMuRepository: DanMuRepository
    private lateinit var inputDialog: InputDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        danMuRepository = DanMuRepository(this)
        mDanMuView = findViewById(R.id.danMuView)
        mStartOrStopButton = findViewById(R.id.btn_startOrStop)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )
        initData()
    }

    private fun initData() {
        val danMuList = danMuRepository.getAllDanMu()
        if (danMuList.isNotEmpty()) {
            mDanMuView.setRow(3)
            mDanMuView.setModels(danMuList.toMutableList())
        } else {
            Toast.makeText(this, "弹幕列表为空，已生成示例弹幕", Toast.LENGTH_SHORT).show()
            generateSampleDanMu()
        }
    }

    private fun generateSampleDanMu() {
        val sampleDanMu = List(50) { "我是一个示例弹幕 $it" }
        mDanMuView.setRow(3)
        mDanMuView.setModels(sampleDanMu.toMutableList())
    }

    fun startDanMu(view: View) {
        if (mDanMuView.getAdapter().getList().isNullOrEmpty()) {
            initData()
        }
        mDanMuView.startPlay()
    }

    fun stopDanMu(view: View) {
        mDanMuView.stopPlay()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearDanMu(view: View) {
        if (mDanMuView.isPlaying()) {
            mStartOrStopButton.text = "启动弹幕"
        } else {
            mStartOrStopButton.text = "停止弹幕"
        }
        mDanMuView.clearModels()
        Log.d("MainActivity", "clearDanMu and list: ${mDanMuView.getAdapter().getList()}")
    }

    fun startOrStopDanMu(view: View) {
        if (mDanMuView.isPlaying()) {
            stopDanMu(view)
            mStartOrStopButton.text = "启动弹幕"
        } else {
            startDanMu(view)
            mStartOrStopButton.text = "停止弹幕"
        }
    }

    fun addDanMu(view: View) {
        inputDialog = InputDialog(this)
        inputDialog.onClickBottomListener=object : InputDialog.OnClickBottomListener{
            override fun onPositiveClick() {
                if (inputDialog.getDanMu().isNotEmpty()){
                    val danMu=inputDialog.getDanMu()
                    insertDanMu(danMu)
                    inputDialog.dismiss()
                } else {
                    Toast.makeText(this@MainActivity, "弹幕内容不能为空", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNegativeClick() {
                inputDialog.dismiss()
            }

        }
        inputDialog.run {
            show()
            danMuEt.requestFocus()
        }
    }

    fun insertDanMu(content: String) {
        // 插入到数据库
        danMuRepository.insertDanMu(content)

        // 获取当前的弹幕列表
        val currentList = mDanMuView.getAdapter().getList()

        // 获取当前 RecyclerView 的最后一个可见位置
        val layoutManager = mDanMuView.rvDanMu?.layoutManager as? StaggeredGridLayoutManager
        val lastVisibleItemPositions = layoutManager?.findLastVisibleItemPositions(null)
        val lastVisiblePosition = lastVisibleItemPositions?.maxOrNull() ?: -1

        // 计算插入位置，确保不越界
        val insertPosition = if (lastVisiblePosition + 1 >= currentList.size) currentList.size else lastVisiblePosition + 1

        // 插入数据到指定位置
        currentList.add(insertPosition, content)

        // 通知 RecyclerView 数据更新
        mDanMuView.getAdapter().apply {
            notifyItemInserted(insertPosition)
            highlightPosition(insertPosition) // 高亮显示
        }

        // 延时移除高亮标识
        mDanMuView.postDelayed({
            mDanMuView.getAdapter().highlightPosition(-1) // 恢复默认样式
        }, 3000) // 高亮持续 3 秒
    }



}