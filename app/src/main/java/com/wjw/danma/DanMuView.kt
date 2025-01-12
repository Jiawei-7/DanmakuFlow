package com.wjw.danma

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.wjw.danma.bean.ColorValue
import com.wjw.danma.database.DanMuRepository
import com.wjw.danma.util.DpUtil

class DanMuView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "DanMuView"
        private const val INTERVAL = 14L
    }

    private var mRow: Int = 1//弹行数
    var rvDanMu: RecyclerView? = null
    private var isPlaying = false
    var size = 16f
    var colorValue = ColorValue(255,235,59)
    private val slideRunnable = object : Runnable {
        override fun run() {
            rvDanMu?.let {
                it.scrollBy(5, 0)// 这里控制滚动速度
                it.postDelayed(this, INTERVAL)
            }
        }
    }
    init {
        LayoutInflater.from(context).inflate(R.layout.layout_danmu_rv, this)
        rvDanMu = findViewById(R.id.rv_dan_mu)
        if(context is ComponentActivity){
            context.lifecycle.addObserver(object : DefaultLifecycleObserver{
                override fun onResume(owner: LifecycleOwner) {
                    startPlay()
                }

                override fun onPause(owner: LifecycleOwner) {
                    stopPlay()
                }
            })
        }
    }

    /**
     * row 弹幕行数
     */
    fun setRow(row: Int) {
        this.mRow = row
    }

    /**
     * @param contentList 弹幕数据
     */
    @SuppressLint("ClickableViewAccessibility")
    fun setModels(contentList: MutableList<String>, startFromEnd: Boolean = true) {
        if (contentList.isEmpty()) return
        val viewAdapter = BarrageAdapter(contentList, mRow, startFromEnd, context, this)
        rvDanMu?.run {
            layoutManager = StaggeredGridLayoutManager(mRow, StaggeredGridLayoutManager.HORIZONTAL)
            adapter = viewAdapter
            setOnTouchListener { _, _ -> true }
        }
    }

    fun isPlaying(): Boolean {
        return isPlaying
    }

    fun stopPlay(){
        removeCallbacks(slideRunnable)
        visibility = VISIBLE
        isPlaying = false
    }

    fun startPlay(){
        removeCallbacks(slideRunnable)
        postDelayed(slideRunnable , INTERVAL)
        visibility = VISIBLE
        isPlaying = true
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearModels() {
        // 停止滚动
        stopPlay()
        visibility = INVISIBLE
        // 清空 RecyclerView 数据
        rvDanMu?.adapter?.let {
            if (it is BarrageAdapter) {
                it.clearData()
            }
        }

        // 刷新 RecyclerView 以清空界面
        rvDanMu?.adapter?.notifyDataSetChanged()
        rvDanMu?.scrollToPosition(0)
    }

    fun setDeleteMode(enabled: Boolean) {
        val adapter = rvDanMu?.adapter as? BarrageAdapter
        adapter?.setDelete(enabled)
    }


    fun getAdapter(): BarrageAdapter {
        return (rvDanMu?.adapter as BarrageAdapter?)!!
    }

    class BarrageAdapter(
        private val dataList: MutableList<String>,
        private val row: Int,
        private val startFromEnd: Boolean,
        private val context: Context,
        private val danMuView: DanMuView
    ) :
        RecyclerView.Adapter<BarrageAdapter.ViewDataHolder>(){
        private var highlightPosition: Int = -1 // 记录需要标识的弹幕位置
        var isDeleteMode = false // 是否处于删除模式
        var danMuRepository = DanMuRepository(context)
        class ViewDataHolder(view: View):RecyclerView.ViewHolder(view){
            val textView: TextView = view.findViewById(R.id.tvText)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewDataHolder {
            return ViewDataHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.danmu_item, parent, false)
            )
        }

        override fun getItemCount(): Int {
            return Int.MAX_VALUE
        }

        fun clearData() {
            dataList.clear()
        }

        fun getList(): MutableList<String> {
            return dataList
        }

        override fun onBindViewHolder(holder: ViewDataHolder, position: Int) {
            if (dataList.isEmpty()) return
            holder.textView.run {
                textSize = danMuView.size
                setTextColor(Color.rgb(danMuView.colorValue.red,danMuView.colorValue.green,danMuView.colorValue.blue))
                val params = layoutParams
                if(startFromEnd){
                    if (position < row){
                        val screenWidth = DpUtil.getScreenWidthInDp(context)
                        params.width = ViewGroup.LayoutParams.WRAP_CONTENT
                        visibility = VISIBLE
                        text = dataList[position % dataList.size]
                    } else{
                        val realIndex = if (position - row > 0) position else 0
                        val textStr = dataList[realIndex % dataList.size]
                        params.width = ViewGroup.LayoutParams.WRAP_CONTENT
                        visibility = if(textStr.isNotEmpty()) VISIBLE else GONE
                        text = textStr
                    }
                    layoutParams = params
                }else{
                    val textStr = dataList[position % dataList.size]
                    visibility = if(textStr.isNotEmpty()) VISIBLE else GONE
                    text = textStr
                }
                // 判断是否需要高亮显示
                if (position % dataList.size == highlightPosition && highlightPosition >=0) {
                    setBackgroundResource(R.drawable.bg_highlight) // 设置带方框的背景
                } else {
                    setBackgroundResource(R.color.white) // 恢复默认背景
                }
                Log.d("HighlightDebug", "Current position: $position, Highlight position: $highlightPosition")
                Log.d("HighlightDebug", "Current list: $dataList")
                Log.d("HighlightDebug", "Current text: $text")

                setOnClickListener {
                    if (isDeleteMode) {
                        Toast.makeText(context, "删除了$text", Toast.LENGTH_SHORT).show()
                        var realPosition = position % dataList.size
                        if (dataList.size>0){
                            danMuRepository.deleteDanMu(dataList[realPosition])
                            dataList.removeAt(realPosition)
                            if (dataList.isEmpty()){
                                Toast.makeText(context, "弹幕列表为清空，已生成示例弹幕", Toast.LENGTH_SHORT).show()
                                danMuView.postDelayed({
                                    val sampleDanMu = List(50) { "我是一个示例弹幕 $it" }
                                    danMuView.setModels(sampleDanMu.toMutableList())
                                },1000)
                            }
                        }
                        while (realPosition<position&&dataList.size>0){
                            notifyItemRemoved(realPosition)
                            notifyItemRangeChanged(realPosition, dataList.size)
                            realPosition+=dataList.size
                        }
                    }
                }
            }
        }

        // 设置需要高亮的弹幕位置
        fun highlightPosition(position: Int) {
            highlightPosition = position
            if (position>=0){
                notifyItemChanged(position)
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        fun setDelete(enabled: Boolean) {
            isDeleteMode = enabled
            notifyDataSetChanged()
        }
    }

}