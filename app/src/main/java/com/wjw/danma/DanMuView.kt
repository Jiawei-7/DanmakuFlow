package com.wjw.danma

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.size
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
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
    fun setModels(contentList: MutableList<String>, startFromEnd:Boolean = true){
        if (contentList.isEmpty()) return
        val viewAdapter = BarrageAdapter(contentList,mRow, startFromEnd)
        rvDanMu?.run {
            layoutManager = StaggeredGridLayoutManager(mRow, StaggeredGridLayoutManager.HORIZONTAL)
            adapter = viewAdapter
            setOnTouchListener { _,_ -> true}
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


    fun getAdapter(): BarrageAdapter {
        return (rvDanMu?.adapter as BarrageAdapter?)!!
    }

    class BarrageAdapter(
        private val dataList: MutableList<String>,
        private val row: Int,
        private val startFromEnd: Boolean
    ) :
        RecyclerView.Adapter<BarrageAdapter.ViewDataHolder>(){
        private var highlightPosition: Int? = null // 记录需要标识的弹幕位置
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
                val params = layoutParams
                if(startFromEnd){
                    if (position < row){
                        val screenWidth = DpUtil.getScreenWidthInDp(context)
                        when(position){
                            1 -> params.width = screenWidth.toInt() + DpUtil.dp2px(context,30f)
                            2 -> params.width = screenWidth.toInt() + DpUtil.dp2px(context,10f)
                            else -> params.width = ViewGroup.LayoutParams.WRAP_CONTENT
                        }
                        visibility = VISIBLE
                        text = dataList[position % dataList.size]
                    } else{
                        val realIndex = if (position - row > 0) position - row else 0
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
                if (position == highlightPosition) {
                    setBackgroundResource(R.drawable.bg_highlight) // 设置带方框的背景
                } else {
                    setBackgroundResource(R.drawable.rounded_bg) // 恢复默认背景
                }
                Log.d("HighlightDebug", "Current position: $position, Highlight position: $highlightPosition")
            }
        }

        // 设置需要高亮的弹幕位置
        fun highlightPosition(position: Int) {
            if (position >= 0){
                highlightPosition = position
                notifyItemChanged(position)
            }
        }
    }

}