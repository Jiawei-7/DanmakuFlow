package com.wjw.danma

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.wjw.danma.bean.ColorValue
import com.wjw.danma.database.DanMuRepository
import com.wjw.danma.util.InputDialog


class MainActivity : AppCompatActivity() {
    private lateinit var mDanMuView: DanMuView
    private lateinit var mStartOrStopButton: Button
    private lateinit var danMuRepository: DanMuRepository
    private lateinit var inputDialog: InputDialog
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var redSeekBar: SeekBar
    private lateinit var greenSeekBar: SeekBar
    private lateinit var blueSeekBar: SeekBar
    private lateinit var spinnerRowCount: Spinner
    private lateinit var fontSizeSpinner: Spinner
    val colorValue = ColorValue()
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
        initSeekBar()
        initRowSpinner()
        initSizeSpinner()
        initData()
        initSetting()
    }

    private fun initSeekBar() {
        redSeekBar = findViewById<SeekBar>(R.id.redSeekBar)
        greenSeekBar = findViewById<SeekBar>(R.id.greenSeekBar)
        blueSeekBar = findViewById<SeekBar>(R.id.blueSeekBar)

        // 设置 SeekBar 改变监听器
        redSeekBar.setOnSeekBarChangeListener(ColorChangeListener())
        greenSeekBar.setOnSeekBarChangeListener(ColorChangeListener())
        blueSeekBar.setOnSeekBarChangeListener(ColorChangeListener())
    }


    private inner class ColorChangeListener : OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            when (seekBar.id) {
                R.id.redSeekBar -> colorValue.red = progress
                R.id.greenSeekBar -> colorValue.green = progress
                R.id.blueSeekBar -> colorValue.blue = progress
            }
            updateColorPreview()
        }

        private fun updateColorPreview() {
            mDanMuView.colorValue = colorValue
            val editor = sharedPreferences.edit()
            editor.putInt("red", colorValue.red)
            editor.putInt("green", colorValue.green)
            editor.putInt("blue", colorValue.blue)
            editor.apply()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        override fun onStopTrackingTouch(seekBar: SeekBar) {}
    }

    private fun initRowSpinner() {
        // 初始化 Spinner
        spinnerRowCount = findViewById(R.id.spinner_row_count)
        val rowOptions = (1..5).map { "$it 行" } // 生成选项文本
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, rowOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRowCount.adapter = adapter

        // 添加选择监听器
        spinnerRowCount.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedRow = position + 1 // 行数
                mDanMuView.setRow(selectedRow) // 设置弹幕行数
                val editor = sharedPreferences.edit()
                editor.putInt("rowCount", selectedRow)
                editor.apply()
                val danMuList = danMuRepository.getAllDanMu()
                if (danMuList.isNotEmpty()) {
                    mDanMuView.setRow(selectedRow)
                    mDanMuView.setModels(danMuList.toMutableList())
                } else {
                    generateSampleDanMu()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 默认行为：不做处理
            }
        }
    }

    private fun initSizeSpinner() {
        fontSizeSpinner = findViewById(R.id.spinner_text_size)
        fontSizeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                val editor = sharedPreferences.edit()
                when (position) {
                    0 -> {
                        mDanMuView.size = 12f
                        editor.putInt("fontSize", 12)
                    }

                    1 -> {
                        mDanMuView.size = 16f
                        editor.putInt("fontSize", 16)
                    }

                    2 -> {
                        mDanMuView.size = 20f
                        editor.putInt("fontSize", 20)
                    }
                }
                editor.apply()
                val danMuList = danMuRepository.getAllDanMu()
                if (danMuList.isNotEmpty()) {
                    mDanMuView.setRow(spinnerRowCount.selectedItemPosition + 1)
                    mDanMuView.setModels(danMuList.toMutableList())
                } else {
                    generateSampleDanMu()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 默认情况下，无操作
            }
        }
    }

    private fun initData() {
        val danMuList = danMuRepository.getAllDanMu()
        if (danMuList.isNotEmpty()) {
            mDanMuView.setRow(spinnerRowCount.selectedItemPosition + 1)
            mDanMuView.setModels(danMuList.toMutableList())
        } else {
            Toast.makeText(this, "弹幕列表为空，已生成示例弹幕", Toast.LENGTH_SHORT).show()
            generateSampleDanMu()
        }
    }

    private fun initSetting() {
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val rowCount = sharedPreferences.getInt("rowCount", 5)
        val fontSize = sharedPreferences.getInt("fontSize", 16)
        val red = sharedPreferences.getInt("red", 255)
        val green = sharedPreferences.getInt("green", 235)
        val blue = sharedPreferences.getInt("blue", 59)
        mDanMuView.setRow(rowCount)
        mDanMuView.size = fontSize.toFloat()
        mDanMuView.colorValue = ColorValue(red, green, blue)
        redSeekBar.progress = red
        greenSeekBar.progress = green
        blueSeekBar.progress = blue
        spinnerRowCount.setSelection(rowCount - 1)
        fontSizeSpinner.setSelection(
            when (fontSize) {
                12 -> 0
                16 -> 1
                else -> 2
            }
        )
    }

    private fun generateSampleDanMu() {
        val sampleDanMu = List(50) { "我是一个示例弹幕 $it" }
        mDanMuView.setRow(spinnerRowCount.selectedItemPosition + 1)
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
        danMuRepository.clearAllDanMu()
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
        inputDialog.onClickBottomListener = object : InputDialog.OnClickBottomListener {
            override fun onPositiveClick() {
                if (inputDialog.getDanMu().isNotEmpty()) {
                    val danMuList = danMuRepository.getAllDanMu()
                    if (danMuList.isEmpty()) {
                        mDanMuView.clearModels()
                    }
                    val danMu = inputDialog.getDanMu()
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
        val insertPosition = if (lastVisiblePosition + 1 >= currentList.size) {
            if (currentList.isEmpty()) {
                startDanMu(mStartOrStopButton)
                0
            } else {
                lastVisiblePosition % currentList.size
            }
        } else lastVisiblePosition

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
        }, 5000) // 高亮持续 3 秒
    }

    fun deleteDanMu(view: View) {
        val isDeleteMode = mDanMuView.getAdapter().isDeleteMode // 当前删除模式状态
        val startOrStopBtn = findViewById<Button>(R.id.btn_startOrStop)
        val clearBtn = findViewById<Button>(R.id.btn_clear)
        val addBtn = findViewById<Button>(R.id.btn_add)
        if (isDeleteMode) {
            // 退出删除模式
            mDanMuView.setDeleteMode(false)
            startOrStopBtn.alpha = 1f
            clearBtn.alpha = 1f
            addBtn.alpha = 1f
            startOrStopBtn.isClickable = true
            clearBtn.isClickable = true
            addBtn.isClickable = true
            (view as Button).text = "删除弹幕"
            Toast.makeText(this, "退出删除模式", Toast.LENGTH_SHORT).show()
        } else {
            // 进入删除模式
            mDanMuView.setDeleteMode(true)
            startOrStopBtn.alpha = 0.3f
            clearBtn.alpha = 0.3f
            addBtn.alpha = 0.3f
            startOrStopBtn.isClickable = false
            clearBtn.isClickable = false
            addBtn.isClickable = false
            (view as Button).text = "取消删除"
            Toast.makeText(this, "进入删除模式，点击弹幕项以删除", Toast.LENGTH_SHORT).show()
        }
    }

}