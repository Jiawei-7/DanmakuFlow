package com.wjw.danma.util

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import com.wjw.danma.R

/**
 * @date   : 2025/01/11 20:10
 * @desc   : 输入对话框
 * @author wjw
 */
class InputDialog(context: Context,val title:String?="请输入",private val positive: String? = "确定",
                  private val negative: String? = "取消") :TextView.OnEditorActionListener,Dialog(context){

    private lateinit var titleTv: TextView
    lateinit var danMuEt: AppCompatEditText
    private lateinit var negativeTv: TextView
    private lateinit var positiveTv: TextView
    var onClickBottomListener: OnClickBottomListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_input)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.setCancelable(false)
        initView()
        initEvent()
    }

    fun initView(){
        titleTv = findViewById(R.id.change_name_title_tv)
        danMuEt = findViewById(R.id.danMu_et)
        negativeTv = findViewById(R.id.tv_cancel)
        positiveTv = findViewById(R.id.tv_confirm)
        titleTv.text=title
    }

    private fun initEvent() {
        //设置确定按钮被点击后，向外界提供监听
        positiveTv.setOnClickListener {
            onClickBottomListener?.onPositiveClick()
        }
        //设置取消按钮被点击后，向外界提供监听
        negativeTv.setOnClickListener {
            onClickBottomListener?.onNegativeClick()
        }
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onClickBottomListener?.onPositiveClick()
            return true
        }
        return false
    }

    /**
     * 点击事件回调
     */
    interface OnClickBottomListener {
        fun onPositiveClick()
        fun onNegativeClick()
    }

    fun getDanMu():String{
        return danMuEt.text.toString()
    }

    fun setText(text:String){
        danMuEt.text=text.toEditable()
    }

    private fun String?.toEditable(): Editable {
        return Editable.Factory.getInstance().newEditable(this ?: "")
    }

    private fun popUpKeyboard() {
        danMuEt.requestFocus() // 强制获取焦点
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(danMuEt, InputMethodManager.SHOW_FORCED)
    }


    override fun show() {
        super.show()
        //延迟弹出键盘
        Handler(Looper.getMainLooper()).postDelayed({
            popUpKeyboard()
        }, 500)
    }


}